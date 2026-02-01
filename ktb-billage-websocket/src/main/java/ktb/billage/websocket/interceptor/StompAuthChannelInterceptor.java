package ktb.billage.websocket.interceptor;

import ktb.billage.common.exception.AuthException;
import ktb.billage.contract.auth.TokenParser;
import ktb.billage.websocket.StompPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import static ktb.billage.common.exception.ExceptionCode.WS_ALREADY_CONNECTED;
import static ktb.billage.common.exception.ExceptionCode.WS_AUTH_TOKEN_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final TokenParser tokenParser;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        String token = resolveAuthHeader(accessor);
        if (token == null) {
            throw new AuthException(WS_AUTH_TOKEN_NOT_FOUND);
        }

        String userId = tokenParser.parseId(token);

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            if (accessor.getUser() != null) {
                throw new AuthException(WS_ALREADY_CONNECTED);
            }
            accessor.setUser(new StompPrincipal(userId));
            return message;
        }

        if (accessor.getUser() == null) {
            accessor.setUser(new StompPrincipal(userId));
        }
        return message;
    }

    private String resolveAuthHeader(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

        if (header == null || header.isBlank()) {
            return null;
        }

        if (header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        return header;
    }

}
