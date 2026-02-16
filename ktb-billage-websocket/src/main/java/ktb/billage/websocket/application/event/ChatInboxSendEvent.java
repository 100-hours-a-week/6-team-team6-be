package ktb.billage.websocket.application.event;

import ktb.billage.websocket.dto.ChatSendAckResponse;

public record ChatInboxSendEvent(
        Long receiveUserId,
        ChatSendAckResponse ack
) {
}
