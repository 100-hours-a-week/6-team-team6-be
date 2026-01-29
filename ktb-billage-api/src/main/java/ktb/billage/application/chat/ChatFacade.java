package ktb.billage.application.chat;

import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.domain.chat.service.ChatMessageQueryService;
import ktb.billage.domain.chat.service.ChatroomCommandService;
import ktb.billage.domain.chat.service.ChatroomQueryService;
import ktb.billage.domain.group.dto.GroupResponse;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.service.PostQueryService;
import ktb.billage.domain.user.dto.UserResponse;
import ktb.billage.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatMessageQueryService chatMessageQueryService;
    private final ChatroomQueryService chatroomQueryService;
    private final ChatroomCommandService chatroomCommandService;
    private final PostQueryService postQueryService;
    private final GroupService groupService;
    private final MembershipService membershipService;
    private final UserService userService;

    public ChatResponse.Messages getMessagesByCursor(Long postId, Long chatroomId, Long userId, String cursor) {
        postQueryService.validatePost(postId);
        Long sellerMembershipId = postQueryService.findSellerIdByPostId(postId);

        Long groupId = membershipService.findGroupIdByMembershipId(sellerMembershipId);
        groupService.validateGroup(groupId);

        Long buyerMembershipId = membershipService.findMembershipId(groupId, userId);

        if (chatroomId == -1L) {
            return chatroomCommandService.create(postId, sellerMembershipId, buyerMembershipId);
        } else {
            return chatMessageQueryService.getMessagesByCursor(chatroomId, buyerMembershipId, cursor);
        }
    }

    public ChatResponse.ChatroomSummaries getChatroomsByMyPostId(Long postId, Long userId, String cursor) {

        postQueryService.validatePost(postId);
        String postFirstImageUrl = postQueryService.findPostFirstImageUrl(postId);

        Long sellerMembershipId = postQueryService.findSellerIdByPostId(postId);
        membershipService.validateMembershipOwner(userId, sellerMembershipId);

        Long groupId = membershipService.findGroupIdByMembershipId(sellerMembershipId);
        groupService.validateGroup(groupId);

        ChatResponse.ChatroomSummaryCores cores = chatroomQueryService.getChatroomSummariesByPostIdAndCursor(postId, cursor);
        List<Long> unreadMessageCounts = chatMessageQueryService.countUnreadPartnerMessagesByAndBetween(cores.chatroomSummaryCores(), sellerMembershipId, true);

        GroupResponse.GroupProfile groupProfile = groupService.findGroupProfile(groupId);

        // TODO. v2에서는 userProfile이 아닌 membershipProfile로 가져와야함.
        List<UserResponse.UserProfile> userProfiles = userService.findUserProfiles(toUserIds(cores.chatroomSummaryCores()));

        List<ChatResponse.ChatroomSummary> summaries = new ArrayList<>();
        for (int i = 0; i < cores.chatroomSummaryCores().size(); i++) {
            summaries.add(new ChatResponse.ChatroomSummary(
                    cores.chatroomSummaryCores().get(i).chatroomId(),
                    cores.chatroomSummaryCores().get(i).chatPartnerId(),
                    userProfiles.get(i).avatarImageUrl(),
                    userProfiles.get(i).nickname(),
                    groupProfile.groupId(),
                    groupProfile.groupName(),
                    postFirstImageUrl,
                    cores.chatroomSummaryCores().get(i).lastMessageAt(),
                    cores.chatroomSummaryCores().get(i).lastMessage(),
                    unreadMessageCounts.get(i)
            ));
        }

        return new ChatResponse.ChatroomSummaries(
                summaries,
                cores.cursorDto()
        );
    }

    public Long countAllUnReadMessagesOnParticipantingChatrooms(Long userId) {
        List<Long> myMembershipIds = membershipService.findMembershipIds(userId);
        if (myMembershipIds.isEmpty()) {
            return 0L;
        }

        List<ChatResponse.ChatroomMembershipDto> myChatroomMemberships = chatroomQueryService.findChatroomIdsByMembershipIds(myMembershipIds);

        Long unreadMessagesCountByMe = chatMessageQueryService.findUnreadMessagesCountByChatInfo(myChatroomMemberships);
        return unreadMessagesCountByMe;
    }

    public ChatResponse.PostSummary getPostSummaryInChatroom(Long postId, Long chatroomId, Long userId) {
        postQueryService.validatePost(postId);
        PostResponse.DetailCore postDetailCore = postQueryService.getPostDetailCore(postId);

        Long sellerMembershipId = postQueryService.findSellerIdByPostId(postId);
        membershipService.validateMembershipOwner(userId, sellerMembershipId);

        Long groupId = membershipService.findGroupIdByMembershipId(sellerMembershipId);
        GroupResponse.GroupProfile groupProfile = groupService.findGroupProfile(groupId);

        chatroomQueryService.validateChatroom(chatroomId);
        ChatResponse.PartnerProfile partnerProfile = chatroomQueryService.findPartnerProfile(chatroomId, sellerMembershipId);

        // TODO. v2 에서는 사용자 닉네임 대신 PartnerProfile에서 한 번에 멤버십 닉네임으로 가져오기.
        Long partnerUserId = membershipService.findUserIdByMembershipId(partnerProfile.partnerId());
        UserResponse.UserProfile partnerUserProfile = userService.findUserProfile(partnerUserId);

        return new ChatResponse.PostSummary(
                partnerProfile.partnerId(),
                partnerUserProfile.nickname(),
                groupId,
                groupProfile.groupName(),
                postId,
                postDetailCore.title(),
                postDetailCore.imageUrls().imageInfos().getFirst().imageUrl(),
                postDetailCore.rentalFee(),
                postDetailCore.feeUnit().name(),
                postDetailCore.rentalStatus().name()
        );
    }

    private List<Long> toUserIds(List<ChatResponse.ChatroomSummaryCore> cores) {
        return cores.stream()
                .mapToLong(ChatResponse.ChatroomSummaryCore::chatPartnerId)
                .map(membershipService::findUserIdByMembershipId)
                .boxed()
                .toList();
    }
}
