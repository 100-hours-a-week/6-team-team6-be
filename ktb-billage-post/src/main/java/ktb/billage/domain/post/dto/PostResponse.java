package ktb.billage.domain.post.dto;

import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.RentalStatus;

import java.math.BigDecimal;
import java.util.List;

public class PostResponse {

    public record Id(Long postId) {}

    public record ChangedStatus(Long postId, RentalStatus rentalStatus) {}

    public record Summaries(
            List<Summary> summaries,
            String nextCursor,
            Boolean hasNextPage
    ) {
    }

    public record Summary(
            Long postId,
            String postTitle,
            Long postImageId,
            String postFirstImageUrl,
            BigDecimal rentalFee,
            FeeUnit feeUnit,
            RentalStatus rentalStatus
    ) {
    }
}
