package ktb.billage.domain.keywordsubscription.dto;

import java.time.Instant;
import java.util.List;

public class KeywordSubscriptionResponse {

    public record Id(
            Long keywordSubscriptionId
    ) {
    }

    public record Summaries(
            List<Summary> keywordSubscriptions
    ) {
    }

    public record Summary(
            Long keywordSubscriptionId,
            String keyword,
            Instant createdAt
    ) {
    }
}
