package ktb.billage.infra.ai;

import io.netty.handler.timeout.TimeoutException;
import ktb.billage.common.exception.AiTimeoutException;
import ktb.billage.common.exception.InternalException;
import ktb.billage.common.exception.PostException;
import ktb.billage.domain.post.ai.AiPostRecommendationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class AiPostRecommendationWebClient implements AiPostRecommendationClient {
    private static final String POST_RECOMMEND_PATH = "/ai/items/recommend";

    private final WebClient webClient;

    @Override
    public List<Long> recommend(Long postId) {
        try {
            RecommendResponse response = webClient.post()
                    .uri(POST_RECOMMEND_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new RecommendRequest(postId))
                    .retrieve()
                    .bodyToMono(RecommendResponse.class)
                    .block();

            if (response == null || response.recommendations() == null) {
                throw new InternalException(SERVER_ERROR);
            }

            return response.recommendations();
        } catch (WebClientResponseException ex) {
            log.error("[AI Server Error Response] path={}, status={}, body={}",
                    POST_RECOMMEND_PATH, ex.getStatusCode(), ex.getResponseBodyAsString());

            if (ex.getStatusCode().value() == 400) {
                throw new PostException(AI_RECOMMENDATION_RETRIEVE_FAILED);
            }
            throw new InternalException(SERVER_ERROR);
        } catch (TimeoutException timeoutException) {
            log.error("[AI Server Exception] Time out");
            throw new AiTimeoutException(TIME_OUT);
        } catch (Exception ex) {
            log.error("[AI Server Exception] Not Handle Error path={}, msg={}", POST_RECOMMEND_PATH, ex.getMessage());
            throw new InternalException(SERVER_ERROR);
        }
    }

    private record RecommendRequest(Long post_id) {
    }

    private record RecommendResponse(List<Long> recommendations) {
    }
}
