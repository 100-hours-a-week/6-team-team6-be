package ktb.billage.websocket.dto;

public record ChatSendRequest(
        Long chatroomId,
        Long membershipId,
        String message
) {
}
