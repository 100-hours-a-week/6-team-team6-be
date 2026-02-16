package ktb.billage.websocket.dto;

public record ChatSendResult(
        ChatSendAckResponse ack,
        Long receiveUserId
) {
}
