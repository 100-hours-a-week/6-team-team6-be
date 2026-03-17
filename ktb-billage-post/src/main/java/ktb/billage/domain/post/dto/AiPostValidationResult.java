package ktb.billage.domain.post.dto;

public record AiPostValidationResult(
        boolean isValid, String policyCode
) {
}
