package ktb.billage.domain.chat.dto;

public record UnreadCountByChatroom(
        Long chatroomId,
        Long unreadCount
) {
}
