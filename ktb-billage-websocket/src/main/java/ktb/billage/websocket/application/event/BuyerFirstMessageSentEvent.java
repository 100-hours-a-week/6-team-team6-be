package ktb.billage.websocket.application.event;

public record BuyerFirstMessageSentEvent(
        Long chatroomId,
        Long buyerMembershipId,
        Long sellerUserId
) {
}
