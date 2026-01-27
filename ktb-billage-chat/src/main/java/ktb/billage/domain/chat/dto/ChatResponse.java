package ktb.billage.domain.chat.dto;

import java.time.Instant;
import java.util.List;

public class ChatResponse {

    public record Messages(
            Long chatroomId,
            List<MessageItem> messageItems,
            String nextCursor,
            Boolean hasNext
            ) {
    }

    public record MessageItem(
            String messageId,
            String who,
            String message,
            Instant createdAt
    ) {
        public MessageItem(Long id, String who, String message, Instant createdAt) {
            this(String.valueOf(id), who, message, createdAt);
        }
    }
}
