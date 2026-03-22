package ktb.billage.infra.ai;

import io.netty.handler.timeout.TimeoutException;
import ktb.billage.common.exception.AiTimeoutException;
import ktb.billage.common.exception.InternalException;
import ktb.billage.common.exception.PostException;
import ktb.billage.domain.post.ai.AiPostRecommendationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.AI_RECOMMENDATION_RETRIEVE_FAILED;
import static ktb.billage.common.exception.ExceptionCode.SERVER_ERROR;
import static ktb.billage.common.exception.ExceptionCode.TIME_OUT;

@Slf4j
@Component
@Profile("!dev & !loadtest")
@RequiredArgsConstructor
public class AiPostRecommendationWebClient implements AiPostRecommendationClient {
    private static final String POST_RECOMMEND_PATH = "/ai/items/recommend";
    private static final String NEED_RECOMMEND_PATH = "/ai/needs/recommend";

    private final WebClient webClient;

    @Override
    public List<Long> recommend(Long postId) {
        return requestRecommendations(POST_RECOMMEND_PATH, new RecommendRequest(postId));
    }

    @Override
    public List<Long> recommendNeeds(Long membershipId) {
        return requestRecommendations(NEED_RECOMMEND_PATH, new NeedRecommendRequest(membershipId));
    }

    private List<Long> requestRecommendations(String path, Object requestBody) {
        try {
            RecommendResponse response = webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(RecommendResponse.class)
                    .block();

            if (response == null || response.recommendations() == null) {
                throw new InternalException(SERVER_ERROR);
            }

            return response.recommendations();
        } catch (WebClientResponseException ex) {
            log.error("[AI Server Error Response] path={}, status={}, body={}",
                    path, ex.getStatusCode(), ex.getResponseBodyAsString());

            if (ex.getStatusCode().value() == 400) {
                throw new PostException(AI_RECOMMENDATION_RETRIEVE_FAILED);
            }
            throw new InternalException(SERVER_ERROR);
        } catch (TimeoutException timeoutException) {
            log.error("[AI Server Exception] Time out");
            throw new AiTimeoutException(TIME_OUT);
        } catch (Exception ex) {
            log.error("[AI Server Exception] Not Handle Error path={}, msg={}", path, ex.getMessage());
            throw new InternalException(SERVER_ERROR);
        }
    }

    private record RecommendRequest(Long post_id) {
    }

    private record NeedRecommendRequest(Long user_id) {
    }

    private record RecommendResponse(List<Long> recommendations) {
    }
}
