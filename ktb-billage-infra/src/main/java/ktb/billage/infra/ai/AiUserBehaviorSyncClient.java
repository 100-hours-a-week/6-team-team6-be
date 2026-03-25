package ktb.billage.infra.ai;

import ktb.billage.application.userbehavior.port.UserBehaviorAiSyncPort;
import ktb.billage.domain.post.userbehavior.UserBehaviorLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@Profile("!loadtest")
@RequiredArgsConstructor
public class AiUserBehaviorSyncClient implements UserBehaviorAiSyncPort {
    private static final String USER_BEHAVIOR_PATH = "/ai/needs/upsert";

    private final WebClient webClient;

    @Override
    public void sync(Long membershipId, Long groupId, List<UserBehaviorLog> recentLogs) {
        UserBehaviorSyncRequest body = new UserBehaviorSyncRequest(
                membershipId,
                groupId,
                recentLogs.stream()
                        .map(log -> new RecentLog(log.getType().name(), log.getContent()))
                        .toList()
        );

        try {
            webClient.post()
                    .uri(USER_BEHAVIOR_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("AI user behavior sync failed. status=" + ex.getStatusCode().value(), ex);
        }
    }

    private record UserBehaviorSyncRequest(
            Long user_id,
            Long group_id,
            List<RecentLog> recent_logs
    ) {}

    private record RecentLog(
            String type,
            String content
    ) {}
}
