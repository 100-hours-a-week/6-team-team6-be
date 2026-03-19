package ktb.billage.application.post;

import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.RentalStatus;
import ktb.billage.domain.post.dto.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PostFeedComposerUnitTest {
    private final PostFeedComposer composer = new PostFeedComposer(ignored -> 0);

    @Test
    @DisplayName("총 20개를 구성할 수 있으면 일반 16개와 추천 4개를 반환한다")
    void composeFirstPageFeed_returnsSixteenBasicsAndFourRecommendations_whenTotalIsTwenty() {
        List<PostResponse.FeedSummary> basicPosts = basicPosts(16);
        List<PostResponse.FeedSummary> recommendations = recommendations(4);

        List<PostResponse.FeedSummary> result = composer.composeFirstPageFeed(basicPosts, recommendations);

        assertThat(result).hasSize(20);
        assertThat(result).filteredOn(item -> item.feedItemType() == PostResponse.FeedItemType.BASIC).hasSize(16);
        assertThat(result).filteredOn(item -> item.feedItemType() == PostResponse.FeedItemType.RECOMMENDATION).hasSize(4);
    }

    @Test
    @DisplayName("20개 미만이면 일반 게시글 대비 추천 게시글 비율은 4대1을 넘지 않는다")
    void composeFirstPageFeed_keepsRecommendationRatioAtMostOnePerFourBasics_whenTotalIsLessThanTwenty() {
        List<PostResponse.FeedSummary> basicPosts = basicPosts(10);
        List<PostResponse.FeedSummary> recommendations = recommendations(5);

        List<PostResponse.FeedSummary> result = composer.composeFirstPageFeed(basicPosts, recommendations);

        assertThat(result).hasSize(12);
        assertThat(result).filteredOn(item -> item.feedItemType() == PostResponse.FeedItemType.BASIC).hasSize(10);
        assertThat(result).filteredOn(item -> item.feedItemType() == PostResponse.FeedItemType.RECOMMENDATION).hasSize(2);
    }

    @Test
    @DisplayName("추천 게시글이 없으면 일반 게시글 20개를 그대로 반환한다")
    void composeFirstPageFeed_returnsBasicPostsOnly_whenRecommendationsAreEmptyAndBasicPostsAreTwenty() {
        List<PostResponse.FeedSummary> basicPosts = basicPosts(20);

        List<PostResponse.FeedSummary> result = composer.composeFirstPageFeed(basicPosts, List.of());

        assertThat(result).hasSize(20);
        assertThat(result).allMatch(item -> item.feedItemType() == PostResponse.FeedItemType.BASIC);
    }

    @Test
    @DisplayName("추천 게시글이 없고 일반 게시글이 더 적으면 그 개수만큼만 반환한다")
    void composeFirstPageFeed_returnsAllBasicPosts_whenRecommendationsAreEmptyAndBasicPostsAreLessThanTwenty() {
        List<PostResponse.FeedSummary> basicPosts = basicPosts(7);

        List<PostResponse.FeedSummary> result = composer.composeFirstPageFeed(basicPosts, List.of());

        assertThat(result).hasSize(7);
        assertThat(result).allMatch(item -> item.feedItemType() == PostResponse.FeedItemType.BASIC);
    }

    private List<PostResponse.FeedSummary> basicPosts(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(index -> new PostResponse.FeedSummary(
                        (long) index,
                        "basic-" + index,
                        (long) index,
                        "img-" + index,
                        BigDecimal.valueOf(index * 1000L),
                        FeeUnit.HOUR,
                        RentalStatus.AVAILABLE,
                        Instant.parse("2026-03-19T00:00:00Z").minusSeconds(index),
                        PostResponse.FeedItemType.BASIC
                ))
                .toList();
    }

    private List<PostResponse.FeedSummary> recommendations(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(index -> new PostResponse.FeedSummary(
                        10_000L + index,
                        "recommendation-" + index,
                        20_000L + index,
                        "rec-img-" + index,
                        BigDecimal.valueOf(index * 2000L),
                        FeeUnit.DAY,
                        RentalStatus.AVAILABLE,
                        Instant.parse("2026-03-19T00:00:00Z").minusSeconds(index),
                        PostResponse.FeedItemType.RECOMMENDATION
                ))
                .toList();
    }
}
