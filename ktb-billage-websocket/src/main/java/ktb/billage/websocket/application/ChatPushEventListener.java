package ktb.billage.websocket.application;

import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatPushNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatPushEventListener {
    private final ChatPushNotifier chatPushNotifier;

    @Async("chatInboxAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatInboxSendEvent event) {
        log.info("ChatPushEventListener Arrvied");
        chatPushNotifier.sendPush(event.receiveUserId(), event.ack());
    }
}
