package ktb.billage.domain.post.dto;

import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.RentalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PostResponse {

    public enum FeedItemType {
        BASIC,
        RECOMMENDATION
    }

    public record Id(Long postId) {}

    public record ChangedStatus(Long postId, RentalStatus rentalStatus) {}

    public record Summaries(
            List<FeedSummary> summaries,
            String nextCursor,
            Boolean hasNextPage
    ) {
    }

    public record FeedSummary(
            Long postId,
            String postTitle,
            Long postImageId,
            String postFirstImageUrl,
            BigDecimal rentalFee,
            FeeUnit feeUnit,
            RentalStatus rentalStatus,
            Instant updatedAt,
            FeedItemType feedItemType
        ) {
    }

    public record MySummaries(
            List<MySummary> summaries,
            String nextCursor,
            Boolean hasNextPage
    ) {
    }

    public record MySummary(
            Long postId,
            String postTitle,
            Long postImageId,
            String postFirstImageUrl,
            Instant updatedAt,
            Long groupId
    ) {
    }

    public record Detail(
        String title,
        String content,
        ImageUrls imageUrls,
        Long sellerId,
        String sellerNickname,
        String sellerAvatar,
        BigDecimal rentalFee,
        FeeUnit feeUnit,
        RentalStatus rentalStatus,
        Instant updatedAt,
        Boolean isSeller,
        Long chatroomId,
        Long activeChatroomCount
    ) {
    }

    public record Recommendations(
            int size,
            List<FeedSummary> recommendations
    ) {
    }

    public record DetailCore(
        String title,
        String content,
        ImageUrls imageUrls,
        Long sellerId,
        BigDecimal rentalFee,
        FeeUnit feeUnit,
        RentalStatus rentalStatus,
        Instant updatedAt
    ) {
    }

    public record ImageUrls(
            List<ImageInfo> imageInfos
    ) {
    }

    public record ImageInfo(
            Long postImageId,
            String imageUrl
    ) {
    }

    public record PostDraft(
            String title,
            String content,
            BigDecimal rentalFee,
            String feeUnit,
            Boolean isRentable
    ) {
    }
}
