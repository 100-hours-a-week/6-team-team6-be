package ktb.billage.websocket;

import ktb.billage.websocket.application.ChatWebSocketFacade;
import ktb.billage.websocket.dto.ChatJoinAckResponse;
import ktb.billage.websocket.dto.ChatJoinRequest;
import ktb.billage.websocket.dto.ChatReadRequest;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import ktb.billage.websocket.dto.ChatSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatWebSocketFacade chatWebSocketFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/join")
    public void join(ChatJoinRequest request, Principal principal) {
        Long userId = parseUserId(principal);
        Long chatroomId = request.chatroomId();

        var participation = chatWebSocketFacade.joinChatroom(chatroomId, userId);
        ChatJoinAckResponse payload = new ChatJoinAckResponse(chatroomId, userId, participation.membershipId());

        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId, payload);
    }

    @MessageMapping("/chat/send")
    public void send(ChatSendRequest request, Principal principal) {
        Long userId = parseUserId(principal);
        Long chatroomId = request.chatroomId();
        Long membershipId = request.membershipId();
        String message = request.message();

        ChatSendAckResponse ack = chatWebSocketFacade.sendMessage(chatroomId, userId, membershipId, message);

        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId, ack);
    }

    @MessageMapping("/chat/read")
    public void read(ChatReadRequest request, Principal principal) {
        Long userId = parseUserId(principal);
        Long chatroomId = request.chatroomId();
        Long membershipId = request.membershipId();
        String messageId = request.readMessageId();

        chatWebSocketFacade.readMessage(chatroomId, userId, membershipId, messageId);
    }

    private Long parseUserId(Principal principal) {
        return Long.parseLong(principal.getName());
    }
}
