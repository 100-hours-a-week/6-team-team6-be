package ktb.billage.websocket;

import ktb.billage.websocket.application.ChatWebSocketFacade;
import ktb.billage.websocket.dto.ChatJoinAckResponse;
import ktb.billage.websocket.dto.ChatJoinRequest;
import ktb.billage.websocket.dto.ChatReadRequest;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import ktb.billage.websocket.dto.ChatSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatWebSocketFacade chatWebSocketFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/join")
    public void join(ChatJoinRequest request, Principal principal) {
        Long userId = parseUserId(principal);
        Long chatroomId = request.chatroomId();
        log.info("[WS /chat/join] userId={}, chatroomId={}", userId, chatroomId);

        var participation = chatWebSocketFacade.joinChatroom(chatroomId, userId);
        ChatJoinAckResponse payload = new ChatJoinAckResponse(chatroomId, participation.membershipId());

        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId, payload);
    }

    @MessageMapping("/chat/send")
    public void send(ChatSendRequest request, Principal principal) {
        Long userId = parseUserId(principal);
        Long chatroomId = request.chatroomId();
        Long membershipId = request.membershipId();
        String message = request.message();
        log.info("[WS /chat/send] userId={}, chatroomId={}, membershipId={}, messageLength={}",
                userId, chatroomId, membershipId, message == null ? 0 : message.length());

        ChatSendAckResponse ack = chatWebSocketFacade.sendMessage(chatroomId, userId, membershipId, message);

        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId, ack);
    }

    @MessageMapping("/chat/read")
    public void read(ChatReadRequest request, Principal principal) {
        Long userId = parseUserId(principal);
        Long chatroomId = request.chatroomId();
        Long membershipId = request.membershipId();
        String messageId = request.readMessageId();
        log.info("[WS /chat/read] userId={}, chatroomId={}, membershipId={}, messageId={}",
                userId, chatroomId, membershipId, messageId);

        chatWebSocketFacade.readMessage(chatroomId, userId, membershipId, messageId);
    }

    private Long parseUserId(Principal principal) {
        return Long.parseLong(principal.getName());
    }
}
