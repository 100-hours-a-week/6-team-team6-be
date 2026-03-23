package ktb.billage.domain.keywordsubscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class KeywordSubscriptionRequest {

    public record Create(
            @NotBlank
            @Size(min = 2, max = 30)
            String keyword
    ) {
    }
}
