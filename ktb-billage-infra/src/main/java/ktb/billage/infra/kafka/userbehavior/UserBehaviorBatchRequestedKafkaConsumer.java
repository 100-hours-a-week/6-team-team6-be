package ktb.billage.infra.kafka.userbehavior;

import ktb.billage.application.userbehavior.UserBehaviorBatchDispatchUseCase;
import ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBehaviorBatchRequestedKafkaConsumer {
    private final UserBehaviorBatchDispatchUseCase useCase;

    @KafkaListener(
            topics = "#{T(ktb.billage.infra.kafka.KafkaTopic).USER_BEHAVIOR_BATCH_REQUESTED.value()}",
            groupId = "user-behavior-batch-requested-consumer",
            properties = {
                    "spring.json.value.default.type=ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent"
            }
    )
    public void consume(@Payload UserBehaviorBatchRequestedEvent event) {
        useCase.handle(event);
    }
}
