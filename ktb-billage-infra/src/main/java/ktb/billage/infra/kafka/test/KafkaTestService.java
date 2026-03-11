package ktb.billage.infra.kafka.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class KafkaTestService {
    private static final String SOURCE = "api-test-controller";

    private final KafkaTestProducer kafkaTestProducer;

    public KafkaTestService(KafkaTestProducer kafkaTestProducer) {
        this.kafkaTestProducer = kafkaTestProducer;
    }

    public KafkaTestMessage publish(String content) {
        KafkaTestMessage message = new KafkaTestMessage(
                UUID.randomUUID().toString(),
                SOURCE,
                content,
                Instant.now()
        );

        log.info("[KAFKA TEST PRODUCER] ----------------");

        kafkaTestProducer.send(message);
        return message;
    }
}
