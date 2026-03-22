package ktb.billage.infra.ai;

import ktb.billage.application.post.event.PostUpsertPayload;
import ktb.billage.application.post.listener.AiSyncPort;
import ktb.billage.application.userbehavior.port.UserBehaviorAiSyncPort;
import ktb.billage.domain.post.ai.AiPostDraftClient;
import ktb.billage.domain.post.ai.AiPostRecommendationClient;
import ktb.billage.domain.post.ai.AiPostValidatorClient;
import ktb.billage.domain.post.dto.AiPostValidationResult;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.userbehavior.UserBehaviorLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Configuration
@Profile({"dev", "loadtest"})
@RequiredArgsConstructor
public class LoadTestAiMockConfig {
    private static final int DEFAULT_RECOMMENDATION_POOL_SIZE = 200;
    private static final int DEFAULT_RECOMMENDATION_RESULT_SIZE = 24;

    private final JdbcTemplate jdbcTemplate;

    @Bean
    @Primary
    public AiPostRecommendationClient loadTestAiPostRecommendationClient(
            @Value("${loadtest.ai.recommendation-pool-size:" + DEFAULT_RECOMMENDATION_POOL_SIZE + "}") int poolSize,
            @Value("${loadtest.ai.recommendation-result-size:" + DEFAULT_RECOMMENDATION_RESULT_SIZE + "}") int resultSize
    ) {
        return new LoadTestAiPostRecommendationClient(jdbcTemplate, Math.max(poolSize, 1), Math.max(resultSize, 1));
    }

    @Bean
    @Primary
    public AiPostValidatorClient loadTestAiPostValidatorClient() {
        return (imageUrls, title, content) -> new AiPostValidationResult(true, null);
    }

    @Bean
    @Primary
    public AiPostDraftClient loadTestAiPostDraftClient() {
        return images -> new PostResponse.PostDraft(
                "loadtest draft title",
                "loadtest draft content",
                BigDecimal.valueOf(1000),
                "HOUR",
                true
        );
    }

    @Bean
    @Primary
    public AiSyncPort loadTestAiSyncPort() {
        return new AiSyncPort() {
            @Override
            public void syncCreated(PostUpsertPayload payload) {
            }

            @Override
            public void syncUpdated(PostUpsertPayload payload) {
            }

            @Override
            public void syncDeleted(Long postId) {
            }
        };
    }

    @Bean
    @Primary
    public UserBehaviorAiSyncPort loadTestUserBehaviorAiSyncPort() {
        return new UserBehaviorAiSyncPort() {
            @Override
            public void sync(Long membershipId, Long groupId, List<UserBehaviorLog> recentLogs) {
            }
        };
    }

    public static class LoadTestAiPostRecommendationClient implements AiPostRecommendationClient {
        private final List<Long> recommendationPool;
        private final int resultSize;

        public LoadTestAiPostRecommendationClient(JdbcTemplate jdbcTemplate, int poolSize, int resultSize) {
            this.resultSize = resultSize;
            this.recommendationPool = Collections.unmodifiableList(
                    jdbcTemplate.queryForList(
                            """
                            select id
                            from post
                            where deleted_at is null
                            order by updated_at desc, id desc
                            limit ?
                            """,
                            Long.class,
                            poolSize
                    )
            );
        }

        @Override
        public List<Long> recommend(Long postId) {
            return slice(postId);
        }

        @Override
        public List<Long> recommendNeeds(Long membershipId) {
            return slice(membershipId);
        }

        private List<Long> slice(Long seed) {
            if (recommendationPool.isEmpty()) {
                return List.of();
            }

            int safeSeed = Math.floorMod(seed == null ? 0 : seed.hashCode(), recommendationPool.size());
            int size = Math.min(resultSize, recommendationPool.size());

            return java.util.stream.IntStream.range(0, size)
                    .mapToObj(index -> recommendationPool.get((safeSeed + index) % recommendationPool.size()))
                    .toList();
        }
    }
}
