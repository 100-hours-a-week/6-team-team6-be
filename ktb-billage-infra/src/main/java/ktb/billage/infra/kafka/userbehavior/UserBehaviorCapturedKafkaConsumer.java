package ktb.billage.infra.kafka.userbehavior;

import ktb.billage.application.userbehavior.UserBehaviorCaptureUseCase;
import ktb.billage.application.userbehavior.event.UserBehaviorCapturedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBehaviorCapturedKafkaConsumer {
    private final UserBehaviorCaptureUseCase useCase;

    @KafkaListener(
            topics = "#{T(ktb.billage.infra.kafka.KafkaTopic).USER_BEHAVIOR_CAPTURED.value()}",
            groupId = "user-behavior-captured-consumer",
            properties = {
                    "spring.json.value.default.type=ktb.billage.application.userbehavior.event.UserBehaviorCapturedEvent"
            }
    )
    public void consume(@Payload UserBehaviorCapturedEvent event) {
        useCase.handle(event);
    }
}
