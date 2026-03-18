package ktb.billage.infra.kafka.userbehavior;

import ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent;
import ktb.billage.application.userbehavior.event.UserBehaviorCapturedEvent;
import ktb.billage.application.userbehavior.port.UserBehaviorEventPublisher;
import ktb.billage.infra.kafka.KafkaTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserBehaviorEventPublisher implements UserBehaviorEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCaptured(UserBehaviorCapturedEvent event) {
        kafkaTemplate.send(KafkaTopic.USER_BEHAVIOR_CAPTURED.value(), String.valueOf(event.membershipId()), event);
    }

    @Override
    public void publishBatchRequested(UserBehaviorBatchRequestedEvent event) {
        kafkaTemplate.send(KafkaTopic.USER_BEHAVIOR_BATCH_REQUESTED.value(), String.valueOf(event.membershipId()), event);
    }
}
