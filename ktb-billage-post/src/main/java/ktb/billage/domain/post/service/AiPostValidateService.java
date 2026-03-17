package ktb.billage.domain.post.service;

import ktb.billage.common.exception.ExceptionCode;
import ktb.billage.common.exception.InternalException;
import ktb.billage.common.exception.PostAiValidateException;
import ktb.billage.domain.post.ai.AiPostValidatorClient;
import ktb.billage.domain.post.dto.AiPostValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.AI_POST_AVERSION;
import static ktb.billage.common.exception.ExceptionCode.AI_POST_PROHIBITED_ITEMS;
import static ktb.billage.common.exception.ExceptionCode.AI_POST_SEXUAL_EXPOSURE;
import static ktb.billage.common.exception.ExceptionCode.AI_POST_SEXUAL_SHAME;
import static ktb.billage.common.exception.ExceptionCode.AI_POST_TORMENT;
import static ktb.billage.common.exception.ExceptionCode.AI_POST_VIOLENCE_VIOLATION;

@Service
@RequiredArgsConstructor
public class AiPostValidateService {
    private final AiPostValidatorClient aiPostValidatorClient;

    public void validateRestrictedItemInPost(List<String> imageUrls, String title, String content) {
        AiPostValidationResult result = aiPostValidatorClient.validatePost(imageUrls, title, content);

        if (result.isValid()) {
            return;
        }

        switch (result.policyCode()) {
            case "S1" -> throw new PostAiValidateException(AI_POST_VIOLENCE_VIOLATION);
            case "S2" -> throw new PostAiValidateException(AI_POST_SEXUAL_SHAME);
            case "S3" -> throw new PostAiValidateException(AI_POST_SEXUAL_EXPOSURE);
            case "S4" -> throw new PostAiValidateException(AI_POST_AVERSION);
            case "S5" -> throw new PostAiValidateException(AI_POST_TORMENT);
            case "S11" -> throw new PostAiValidateException(AI_POST_PROHIBITED_ITEMS);
            default -> throw new InternalException(ExceptionCode.SERVER_ERROR);
        }
    }
}
