package ktb.billage.infra.kafka.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTestProducer {
    public static final String TOPIC = "billage.test";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(KafkaTestMessage message) {
        log.info("-------------------Kafka Producer Send ---------");

        kafkaTemplate.send(TOPIC, message.messageId(), message);
    }
}
