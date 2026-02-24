package ktb.billage.websocket.application;

import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatWebSocketNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatWebSocketEventListener {
    private final ChatWebSocketNotifier chatWebSocketNotifier;

    @Async("chatInboxAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatInboxSendEvent event) {
        chatWebSocketNotifier.sendToUserInbox(event.receiveUserId(), event.ack());
    }
}
