package ktb.billage.infra.ai;

import io.netty.handler.timeout.TimeoutException;
import ktb.billage.common.exception.AiTimeoutException;
import ktb.billage.common.exception.InternalException;
import ktb.billage.domain.post.ai.AiPostValidatorClient;
import ktb.billage.domain.post.dto.AiPostValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.SERVER_ERROR;
import static ktb.billage.common.exception.ExceptionCode.TIME_OUT;

@Slf4j
@Component
@Profile("!dev & !loadtest")
@RequiredArgsConstructor
public class AiPostValidatorWebClient implements AiPostValidatorClient {
    private static final String POST_VALIDATE_PATH = "/ai/validator";
    private static final String SAFE = "safe";

    private final WebClient webClient;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Override
    public AiPostValidationResult validatePost(List<String> imageUrls, String title, String content) {
        String requestUrl = baseUrl + POST_VALIDATE_PATH;
        ValidatePostResponse response;

        try {
            response = webClient.post()
                    .uri(POST_VALIDATE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ValidatePostRequest(
                            imageUrls.stream()
                                    .map(ImageRequest::new)
                                    .toList(),
                            title,
                            content
                    ))
                    .retrieve()
                    .bodyToMono(ValidatePostResponse.class)
                    .block();

            if (response == null) {
                throw new InternalException(SERVER_ERROR);
            }

            if (response.isSafe()) {
                return new AiPostValidationResult(true, null);
            } else {
                return new AiPostValidationResult(false, response.policyCode());
            }
        } catch (WebClientResponseException e) {
            log.error("[AI Server Error Response] url={}, status={}, body={}",
                    requestUrl, e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalException(SERVER_ERROR);
        } catch (TimeoutException timeoutException) {
            log.error("[AI Server Exception] Time out");
            throw new AiTimeoutException(TIME_OUT);
        } catch (Exception e) {
            log.error("[AI Server Exception] Not Handle Error url={}, msg={}", requestUrl, e.getMessage());
            throw new InternalException(SERVER_ERROR);
        }
    }

    private record ValidatePostRequest(
            List<ImageRequest> images,
            String title,
            String content
    ) {
    }

    private record ImageRequest(
            String image
    ) {
    }

    private record ValidatePostResponse(
            String is_safe,
            String policy_code
    ) {
        private boolean isSafe() {
            return SAFE.equals(is_safe);
        }

        private String policyCode() {
            return policy_code;
        }
    }
}
