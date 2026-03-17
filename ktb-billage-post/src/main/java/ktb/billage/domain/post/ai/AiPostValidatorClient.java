package ktb.billage.domain.post.ai;

import ktb.billage.domain.post.dto.AiPostValidationResult;

import java.util.List;

public interface AiPostValidatorClient {

    AiPostValidationResult validatePost(List<String> imageUrls, String title, String content);
}
