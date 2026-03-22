package ktb.billage.infra.ai;

import ktb.billage.application.post.event.PostUpsertPayload;
import ktb.billage.application.post.listener.AiSyncPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Component
@Profile("!dev & !loadtest")
@RequiredArgsConstructor
public class AiPostSyncClient implements AiSyncPort {
    private static final String POST_UPSERT_PATH = "/ai/items/upsert";
    private static final String POST_DELETE_PATH = "/ai/items/{post_id}";

    private final WebClient webClient;

    @Override
    public void syncCreated(PostUpsertPayload payload) {
        PostSyncRequest body = toRequestBody(payload);

        postToAiServer(body);
    }

    @Override
    public void syncUpdated(PostUpsertPayload payload) {
        PostSyncRequest body = toRequestBody(payload);

        postToAiServer(body);
    }

    private void postToAiServer(PostSyncRequest request) {
        try {
            webClient.post()
                    .uri(POST_UPSERT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw mapUpsertException(ex, request.post_id());
        }
    }

    @Override
    public void syncDeleted(Long postId) {
        try {
            webClient.delete()
                    .uri(POST_DELETE_PATH, postId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw mapDeleteException(ex, postId);
        }
    }

    private record PostSyncRequest(
            Long user_id,
            Long group_id,
            Long post_id,
            String file_key,
            String title,
            BigDecimal price,
            String price_unit
    ) {}

    private PostSyncRequest toRequestBody(PostUpsertPayload payload) {
        return new PostSyncRequest(
                payload.membershipId(),
                payload.groupId(),
                payload.postId(),
                payload.imageUrl(),
                payload.title(),
                payload.price(),
                payload.feeUnit()
        );
    }

    private RuntimeException mapUpsertException(WebClientResponseException ex, Long postId) {
        int status = ex.getStatusCode().value();
        if (status == 400) {
            return new IllegalStateException("AI upsert bad request(400). postId=" + postId, ex);
        }
        if (status == 404) {
            return new IllegalStateException("AI upsert invalid file_key(404). postId=" + postId, ex);
        }
        if (status == 500) {
            return new IllegalStateException("AI upsert qdrant issue(500). postId=" + postId, ex);
        }
        return new IllegalStateException("AI upsert failed. status=" + status + ", postId=" + postId, ex);
    }

    private RuntimeException mapDeleteException(WebClientResponseException ex, Long postId) {
        int status = ex.getStatusCode().value();
        if (status == 404) {
            return new IllegalStateException("AI delete invalid post_id(404). postId=" + postId, ex);
        }
        if (status == 500) {
            return new IllegalStateException("AI delete qdrant issue(500). postId=" + postId, ex);
        }
        return new IllegalStateException("AI delete failed. status=" + status + ", postId=" + postId, ex);
    }
}
