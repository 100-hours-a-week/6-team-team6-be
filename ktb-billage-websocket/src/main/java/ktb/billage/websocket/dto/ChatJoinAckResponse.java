package ktb.billage.websocket.dto;

public record ChatJoinAckResponse(Long chatroomId, Long userId, Long membershipId) {
}
