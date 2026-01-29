package ktb.billage.domain.chat.dto;

import java.time.Instant;
import java.util.List;

public class ChatResponse {

    public record CursorDto(String cursor, Boolean hasNext) {
    }

    public record Messages(
            Long chatroomId,
            List<MessageItem> messageItems,
            CursorDto cursorDto
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

    public record ChatroomSummaries(
            List<ChatroomSummary> chatroomSummaries,
            CursorDto cursorDto
    ) {
    }

    public record ChatroomSummary(
            Long chatroomId,
            Long chatPartnerId,
            String chatPartnerAvatarUrl,
            String chatPartnerNickname,
            Long groupId,
            String groupName,
            String postFirstImageUrl,
            Instant lastMessageAt,
            String lastMessage,
            Long unreadCount
    ) {
    }

    public record ChatroomSummaryCores(
            List<ChatroomSummaryCore> chatroomSummaryCores,
            CursorDto cursorDto
    ) {
    }

    public record ChatroomSummaryCore(
            Long chatroomId,
            Long chatPartnerId,
            Long lastMessageId,
            Instant lastMessageAt,
            String lastMessage,
            Long sellerLastReadMessageId,
            Long buyerLastReadMessageId
    ) {
    }

    public record ChatroomMembershipDto(
            Long chatroomId,
            Long membershipId,
            boolean isSeller
    ) {
    }
}
