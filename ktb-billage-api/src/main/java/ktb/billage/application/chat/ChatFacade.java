package ktb.billage.application.chat;

import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.domain.chat.service.ChatMessageQueryService;
import ktb.billage.domain.chat.service.ChatroomCommandService;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.post.service.PostCommandService;
import ktb.billage.domain.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatMessageQueryService chatMessageQueryService;
    private final ChatroomCommandService chatroomCommandService;
    private final PostQueryService postQueryService;
    private final GroupService groupService;
    private final MembershipService membershipService;
    private final PostCommandService postCommandService;

    public ChatResponse.Messages getMessagesByCursor(Long postId, Long chatroomId, Long userId, String cursor) {
        postCommandService.validatePost(postId);
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
}
