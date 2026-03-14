package ktb.billage.infra.kafka.post;

import ktb.billage.application.post.event.PostCreateEvent;
import ktb.billage.application.post.port.PostEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPostEventPublisher implements PostEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPostEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishPostCreated(PostCreateEvent event) {
        kafkaTemplate.send(PostKafkaTopic.CREATED.value(), String.valueOf(event.payload().postId()), event);
    }
}
