package ktb.billage.websocket.application;

import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatPushNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatPushEventListener {
    private final ChatPushNotifier chatPushNotifier;

    @Async("chatInboxAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatInboxSendEvent event) {
        chatPushNotifier.sendPush(event.receiveUserId(), event.ack());
    }
}
