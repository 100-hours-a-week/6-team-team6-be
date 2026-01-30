package ktb.billage.application.post;

import ktb.billage.domain.post.RentalStatus;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.service.PostCommandService;
import ktb.billage.domain.post.service.PostQueryService;
import ktb.billage.domain.chat.service.ChatroomQueryService;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.user.dto.UserResponse;
import ktb.billage.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostFacade {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final GroupService groupService;
    private final MembershipService membershipService;
    private final ChatroomQueryService chatroomQueryService;
    private final UserService userService;

    public PostResponse.Id create(Long groupId, Long userId, PostRequest.Create request) {
        groupService.validateGroup(groupId);
        Long membershipId = membershipService.findMembershipId(groupId, userId);
        return postCommandService.create(
                membershipId,
                request.title(),
                request.content(),
                request.imageUrls(),
                request.rentalFee(),
                request.feeUnit()
        );
    }

    public PostResponse.Id update(Long groupId, Long postId, Long userId, PostRequest.Update request) {
        groupService.validateGroup(groupId);
        Long membershipId = membershipService.findMembershipId(groupId, userId);
        return postCommandService.update(
                postId,
                membershipId,
                request.title(),
                request.content(),
                request.imageUrls(),
                request.rentalFee(),
                request.feeUnit()
        );
    }

    public PostResponse.ChangedStatus changeRentalStatus(Long groupId, Long postId, Long userId, RentalStatus rentalStatus) {
        groupService.validateGroup(groupId);
        Long membershipId = membershipService.findMembershipId(groupId, userId);
        return postCommandService.changeRentalStatus(postId, membershipId, rentalStatus);
    }

    public void delete(Long groupId, Long postId, Long userId) {
        groupService.validateGroup(groupId);
        Long membershipId = membershipService.findMembershipId(groupId, userId);
        postCommandService.delete(postId, membershipId);
    }

    public PostResponse.Summaries getPostsByCursor(Long groupId, Long userId, String cursor) {
        groupService.validateGroup(groupId);
        membershipService.validateMembership(groupId, userId);
        return postQueryService.getPostsByCursor(cursor);
    }

    public PostResponse.Summaries getPostsByKeywordAndCursor(Long groupId, Long userId, String keyword, String cursor) {
        groupService.validateGroup(groupId);
        membershipService.validateMembership(groupId, userId);
        return postQueryService.getPostsByKeywordAndCursor(keyword, cursor);
    }

    public PostResponse.Detail getPostDetail(Long groupId, Long postId, Long userId) {
        groupService.validateGroup(groupId);
        Long membershipId = membershipService.findMembershipId(groupId, userId);
        PostResponse.DetailCore core = postQueryService.getPostDetailCore(postId);
        boolean isSeller = core.sellerId().equals(membershipId);

        Long sellerUserId = membershipService.findUserIdByMembershipId(core.sellerId());
        UserResponse.UserProfile sellerProfile = userService.findUserProfile(sellerUserId);

        Long chatroomId;
        Long activeChatroomCount;

        if (isSeller) {
            chatroomId = null;
            activeChatroomCount = chatroomQueryService.countChatroomsByPostId(postId);
        } else {
            chatroomId = chatroomQueryService.findChatroomIdByPostIdAndBuyerId(postId, membershipId);
            activeChatroomCount = null;
        }

        return new PostResponse.Detail(
                core.title(),
                core.content(),
                core.imageUrls(),
                core.sellerId(),
                sellerProfile.nickname(),
                sellerProfile.avatarImageUrl(),
                core.rentalFee(),
                core.feeUnit(),
                core.rentalStatus(),
                core.updatedAt(),
                isSeller,
                chatroomId,
                activeChatroomCount
        );
    }
}
