package ktb.billage.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class StompLifecycleLoggingInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        log.info("[STOMP {}] sessionId={}, user={}, destination={}, headers={}, payloadType={}",
                command,
                accessor.getSessionId(),
                accessor.getUser(),
                accessor.getDestination(),
                safeNativeHeaders(accessor),
                payloadType(message));

        return message;
    }

    private Map<String, ?> safeNativeHeaders(StompHeaderAccessor accessor) {
        Map<String, ?> nativeHeaders = accessor.toNativeHeaderMap();
        return nativeHeaders == null ? Map.of() : nativeHeaders;
    }

    private String payloadType(Message<?> message) {
        Object payload = message.getPayload();
        if (payload == null) {
            return "null";
        }
        return payload.getClass().getName();
    }
}
