package ktb.billage.websocket.interceptor;

import ktb.billage.websocket.application.ChatWebSocketFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class ChatroomSubscriptionInterceptor implements ChannelInterceptor {
    private static final String CHATROOM_TOPIC_PREFIX = "/topic/chatrooms/";

    private final ChatWebSocketFacade chatWebSocketFacade;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(CHATROOM_TOPIC_PREFIX)) {
            return message;
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            return message;
        }

        String roomIdText = destination.substring(CHATROOM_TOPIC_PREFIX.length());
        Long chatroomId = Long.parseLong(roomIdText);
        Long userId = Long.parseLong(principal.getName());

        chatWebSocketFacade.validateParticipating(chatroomId, userId);

        return message;
    }
}
