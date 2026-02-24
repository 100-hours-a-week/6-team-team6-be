package ktb.billage.infra.fcm;

import ktb.billage.websocket.application.port.ChatPushNotifier;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpChatPushNotifier implements ChatPushNotifier {
    @Override
    public void sendPush(Long receiveUserId, ChatSendAckResponse ack) {
        log.debug("FCM disabled. skip push notification. receiveUserId={}, chatroomId={}, messageId={}",
                receiveUserId, ack.chatroomId(), ack.messageId());
    }
}
