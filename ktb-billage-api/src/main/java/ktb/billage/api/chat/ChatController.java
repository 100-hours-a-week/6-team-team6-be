package ktb.billage.api.chat;

import ktb.billage.application.chat.ChatFacade;
import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatFacade chatFacade;

    @GetMapping("/posts/{postId}/chatrooms/{chatroomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long postId, @PathVariable Long chatroomId,
                                         @AuthenticatedId Long userId, @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok()
                .body(chatFacade.getMessagesByCursor(postId, chatroomId, userId, cursor));
    }

    @GetMapping("/users/me/posts/{postId}/chatrooms")
    public ResponseEntity<?> getChatroomsByMyPostId(@PathVariable Long postId, @AuthenticatedId Long userId,
                                                    @RequestParam(required = false) String cursor) {

        ChatResponse.ChatroomSummaries summaries = chatFacade.getChatroomsByMyPostId(postId, userId, cursor);
        return ResponseEntity.ok().body(summaries);
    }

    @GetMapping("/users/me/chatrooms/unread-count")
    public ResponseEntity<?> getUnreadMessageCountOnMyAllChatrooms(@AuthenticatedId Long userId) {

        Long unreadCount = chatFacade.countAllUnReadMessagesOnParticipatingChatrooms(userId);
        return ResponseEntity.ok().body(Map.of("unreadChatMesageCount", unreadCount));
    }

    @GetMapping("/posts/{postId}/chatrooms/{chatroomId}/post")
    public ResponseEntity<?> getPostSummaryInChatroom(@PathVariable Long postId, @PathVariable Long chatroomId,
                                                      @AuthenticatedId Long userId) {
        ChatResponse.PostSummary postSummary = chatFacade.getPostSummaryInChatroom(postId, chatroomId, userId);
        return ResponseEntity.ok().body(postSummary);
    }
}
