package ktb.billage.websocket.application;

import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatInboxNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatInboxEventListener {
    private final ChatInboxNotifier chatInboxNotifier;

    @Async("chatInboxAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatInboxSendEvent event) {
        chatInboxNotifier.sendToUserInbox(event.receiveUserId(), event.ack());
    }
}
