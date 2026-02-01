package ktb.billage.websocket.exception;

import ktb.billage.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable root = unwrap(ex);
        if (root instanceof BaseException baseException) {
            return buildErrorMessage(baseException.getCode());
        }

        return buildErrorMessage("SERVER_ERROR");
    }

    private Message<byte[]> buildErrorMessage(String code) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);

        byte[] payload = toJson(code);
        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }

    private byte[] toJson(String code) {
        try {
            return objectMapper.writeValueAsBytes(new ErrorPayload(code));
        } catch (JacksonException e) {
            return ("{\"code\":\"" + code + "\"}").getBytes();
        }
    }

    private Throwable unwrap(Throwable ex) {
        if (ex instanceof MessageDeliveryException mde && mde.getCause() != null) {
            return mde.getCause();
        }
        return ex;
    }

    private record ErrorPayload(String code) {
    }
}
