package ktb.billage.infra.kafka.test;

import java.time.Instant;

public record KafkaTestMessage(
        String messageId,
        String source,
        String content,
        Instant createdAt
) {
}
