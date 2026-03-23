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
import java.util.List;

@Configuration
@Profile({"dev", "loadtest"})
@RequiredArgsConstructor
public class LoadTestAiMockConfig {
    private static final int DEFAULT_RECOMMENDATION_RESULT_SIZE = 5;

    private final JdbcTemplate jdbcTemplate;

    @Bean
    @Primary
    public AiPostRecommendationClient loadTestAiPostRecommendationClient(
            @Value("${loadtest.ai.recommendation-result-size:" + DEFAULT_RECOMMENDATION_RESULT_SIZE + "}") int resultSize
    ) {
        return new LoadTestAiPostRecommendationClient(jdbcTemplate, Math.max(resultSize, 1));
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
        private static final String FIND_GROUP_ID_BY_POST_ID = """
                select m.group_id
                from post p
                join membership m on m.id = p.membership_id
                where p.id = ?
                  and p.deleted_at is null
                """;
        private static final String FIND_GROUP_ID_BY_MEMBERSHIP_ID = """
                select group_id
                from membership
                where id = ?
                  and deleted_at is null
                """;
        private static final String FIND_ACTIVE_POST_IDS_BY_GROUP_ID = """
                select id
                from (
                    select p.id, p.updated_at
                    from post p
                    join membership m on m.id = p.membership_id
                    where m.group_id = ?
                      and p.deleted_at is null
                    order by p.updated_at desc, p.id desc
                    limit ?
                ) recent_posts
                order by updated_at desc, id desc
                limit ?
                """;

        private final JdbcTemplate jdbcTemplate;
        private final int resultSize;

        public LoadTestAiPostRecommendationClient(JdbcTemplate jdbcTemplate, int resultSize) {
            this.jdbcTemplate = jdbcTemplate;
            this.resultSize = resultSize;
        }

        @Override
        public List<Long> recommend(Long postId) {
            Long groupId = findGroupIdByPostId(postId);
            if (groupId == null) {
                return List.of();
            }
            return findRecentActivePostIdsByGroupId(groupId);
        }

        @Override
        public List<Long> recommendNeeds(Long membershipId) {
            Long groupId = findGroupIdByMembershipId(membershipId);
            if (groupId == null) {
                return List.of();
            }
            return findRecentActivePostIdsByGroupId(groupId);
        }

        private Long findGroupIdByPostId(Long postId) {
            return jdbcTemplate.query(
                    FIND_GROUP_ID_BY_POST_ID,
                    rs -> rs.next() ? rs.getLong("group_id") : null,
                    postId
            );
        }

        private Long findGroupIdByMembershipId(Long membershipId) {
            return jdbcTemplate.query(
                    FIND_GROUP_ID_BY_MEMBERSHIP_ID,
                    rs -> rs.next() ? rs.getLong("group_id") : null,
                    membershipId
            );
        }

        private List<Long> findRecentActivePostIdsByGroupId(Long groupId) {
            return jdbcTemplate.queryForList(
                    FIND_ACTIVE_POST_IDS_BY_GROUP_ID,
                    Long.class,
                    groupId,
                    resultSize,
                    resultSize
            );
        }
    }
}
