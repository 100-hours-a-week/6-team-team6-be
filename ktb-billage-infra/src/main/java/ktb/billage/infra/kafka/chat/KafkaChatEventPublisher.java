package ktb.billage.infra.kafka.chat;

import ktb.billage.infra.kafka.KafkaTopic;
import ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent;
import ktb.billage.websocket.application.port.ChatEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaChatEventPublisher implements ChatEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishFirstMessageSent(BuyerFirstMessageSentEvent event) {
        kafkaTemplate.send(KafkaTopic.FIRST_CHAT_SENT.value(), String.valueOf(event.chatroomId()), event);
    }
}
