package ktb.billage.websocket.dto;

public record ChatReadRequest(
    Long chatroomId,
    Long membershipId,
    String readMessageId
) {
}
