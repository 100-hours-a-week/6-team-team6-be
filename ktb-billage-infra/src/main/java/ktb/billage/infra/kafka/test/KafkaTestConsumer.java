package ktb.billage.infra.kafka.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaTestConsumer {

    @KafkaListener(
            topics = KafkaTestProducer.TOPIC,
            groupId = "billage-test-consumer",
            properties = {
                    "spring.json.value.default.type=ktb.billage.infra.kafka.test.KafkaTestMessage"
            }
    )
    public void consume(@Payload KafkaTestMessage message) {
        log.info("[KAFKA TEST CONSUMER] topic={}, messageId={}, source={}, content={}, createdAt={}",
                KafkaTestProducer.TOPIC,
                message.messageId(),
                message.source(),
                message.content(),
                message.createdAt());
    }
}
