package ktb.billage.domain.post.dto;

import ktb.billage.domain.post.RentalStatus;

public class PostResponse {

    public record Id(Long postId) {}

    public record ChangedStatus(Long postId, RentalStatus rentalStatus) {}
}
