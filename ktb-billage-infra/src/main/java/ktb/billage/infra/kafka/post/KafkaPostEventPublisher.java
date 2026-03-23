package ktb.billage.infra.kafka.post;

import ktb.billage.application.post.event.PostCreateEvent;
import ktb.billage.application.post.port.PostEventPublisher;
import ktb.billage.infra.kafka.KafkaTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPostEventPublisher implements PostEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishPostCreated(PostCreateEvent event) {
        kafkaTemplate.send(KafkaTopic.POST_CREATED.value(), String.valueOf(event.payload().postId()), event);
    }
}
