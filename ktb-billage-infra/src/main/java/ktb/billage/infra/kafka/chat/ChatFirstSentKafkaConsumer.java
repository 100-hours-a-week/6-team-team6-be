package ktb.billage.infra.kafka.chat;

import ktb.billage.application.chat.ChatFirstSentNotificationUseCase;
import ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFirstSentKafkaConsumer {
    private final ChatFirstSentNotificationUseCase useCase;

    @KafkaListener(
            topics = "#{T(ktb.billage.infra.kafka.KafkaTopic).FIRST_CHAT_SENT.value()}",
            groupId = "chat-first-sent-notification-consumer",
            properties = {
                    "spring.json.value.default.type=ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent"
            }
    )
    public void consume(@Payload BuyerFirstMessageSentEvent event) {
        useCase.handle(event);
    }
}
