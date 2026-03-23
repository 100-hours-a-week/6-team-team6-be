package ktb.billage.infra.kafka.post;

import ktb.billage.application.keywordsubscription.PostCreatedKeywordNotificationUseCase;
import ktb.billage.application.post.event.PostCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCreatedKafkaConsumer {
    private final PostCreatedKeywordNotificationUseCase useCase;

    @KafkaListener(
            topics = "#{T(ktb.billage.infra.kafka.KafkaTopic).POST_CREATED.value()}",
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
