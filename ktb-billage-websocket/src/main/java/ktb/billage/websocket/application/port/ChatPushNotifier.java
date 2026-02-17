package ktb.billage.websocket.application.port;

import ktb.billage.websocket.dto.ChatSendAckResponse;

public interface ChatPushNotifier {
    void sendPush(Long receiveUserId, ChatSendAckResponse ack);
}
