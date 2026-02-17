package ktb.billage.websocket.application.port;

import ktb.billage.websocket.dto.ChatSendAckResponse;

public interface ChatWebSocketNotifier {
    void sendToUserInbox(Long receiveUserId, ChatSendAckResponse ack);
}
