package ktb.billage.infra.kafka.post;

import ktb.billage.application.keywordsubscription.PostCreatedKeywordNotificationUseCase;
import ktb.billage.application.post.event.PostCreateEvent;
import ktb.billage.application.post.event.PostUpsertPayload;
import ktb.billage.infra.kafka.KafkaConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
        KafkaConfig.class,
        KafkaPostEventPublisher.class,
        PostCreatedKafkaConsumer.class,
        PostKafkaIntegrationTest.TestKafkaConfig.class
})
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = "post.created"
)
class PostKafkaIntegrationTest {

    @Autowired
    private KafkaPostEventPublisher kafkaPostEventPublisher;

    @Autowired
    private PostCreatedKeywordNotificationUseCase postCreatedKeywordNotificationUseCase;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private Consumer<String, byte[]> testConsumer;

    @AfterEach
    void tearDown() {
        clearInvocations(postCreatedKeywordNotificationUseCase);

        if (testConsumer != null) {
            testConsumer.close();
            testConsumer = null;
        }
    }

    @Test
    @DisplayName("게시글 생성 이벤트를 발행하면 카프카 토픽에 적재된다")
    void publishPostCreated_sendsMessageToKafkaTopic() throws Exception {
        PostCreateEvent event = createEvent();

        testConsumer = createPublisherTestConsumer("publisher-test-group-" + UUID.randomUUID());
        TopicPartition topicPartition = new TopicPartition(PostKafkaTopic.CREATED.value(), 0);
        testConsumer.assign(Collections.singleton(topicPartition));
        long startOffset = testConsumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
        testConsumer.seek(topicPartition, startOffset);

        kafkaPostEventPublisher.publishPostCreated(event);
        kafkaTemplate.flush();

        ConsumerRecord<String, byte[]> matchingRecord = null;
        long deadline = System.currentTimeMillis() + 5_000;
        String target = String.valueOf(event.payload().postId());
        while (matchingRecord == null && System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, byte[]> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(500));
            for (ConsumerRecord<String, byte[]> record : records.records(PostKafkaTopic.CREATED.value())) {
                if (record.offset() >= startOffset
                        && target.equals(record.key())) {
                    matchingRecord = record;
                    break;
                }
            }
        }

        if (matchingRecord == null) {
            throw new AssertionError("Published event was not found in Kafka topic");
        }
        PostCreateEvent actualEvent = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(matchingRecord.value(), PostCreateEvent.class);

        assertThat(matchingRecord.key()).isEqualTo(target);
        assertThat(actualEvent).isEqualTo(event);
    }

    @Test
    @DisplayName("게시글 생성 이벤트가 카프카로 들어오면 컨슈머가 유즈케이스를 호출한다")
    void consumePostCreated_invokesUseCase() {
        PostCreateEvent event = createEvent();

        kafkaPostEventPublisher.publishPostCreated(event);

        verify(postCreatedKeywordNotificationUseCase, timeout(5_000))
                .handle(argThat(actual -> actual != null && actual.equals(event)));
    }

    private Consumer<String, byte[]> createPublisherTestConsumer(String groupId) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        DefaultKafkaConsumerFactory<String, byte[]> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProps,
                        new StringDeserializer(),
                        new ByteArrayDeserializer()
                );
        return consumerFactory.createConsumer();
    }

    private PostCreateEvent createEvent() {
        return new PostCreateEvent(new PostUpsertPayload(
                1L,
                2L,
                3L,
                "image-key",
                "드릴 빌려드립니다",
                BigDecimal.valueOf(10_000),
                "HOUR"
        ));
    }

    @TestConfiguration
    static class TestKafkaConfig {
        @Bean
        PostCreatedKeywordNotificationUseCase postCreatedKeywordNotificationUseCase() {
            return mock(PostCreatedKeywordNotificationUseCase.class);
        }
    }
}
