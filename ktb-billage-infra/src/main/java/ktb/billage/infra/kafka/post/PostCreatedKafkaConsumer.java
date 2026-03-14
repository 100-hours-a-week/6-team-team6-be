package ktb.billage.infra.kafka.post;

import ktb.billage.application.keywordsubscription.PostCreatedKeywordNotificationUseCase;
import ktb.billage.application.post.event.PostCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PostCreatedKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(PostCreatedKafkaConsumer.class);

    private final PostCreatedKeywordNotificationUseCase useCase;

    public PostCreatedKafkaConsumer(PostCreatedKeywordNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(
            topics = "#{T(ktb.billage.infra.kafka.post.PostKafkaTopic).CREATED.value()}",
            groupId = "post-created-keyword-notification-consumer",
            properties = {
                    "spring.json.value.default.type=ktb.billage.application.post.event.PostCreateEvent"
            }
    )
    public void consume(@Payload PostCreateEvent event) {
        log.info("[POST CREATED KAFKA CONSUMER] postId={}, groupId={}, title={}",
                event.payload().postId(),
                event.payload().groupId(),
                event.payload().title());
        useCase.handle(event);
    }
}
