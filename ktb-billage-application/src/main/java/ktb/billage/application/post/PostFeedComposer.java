package ktb.billage.application.post;

import ktb.billage.domain.post.dto.PostResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

final class PostFeedComposer {
    static final int POST_PAGE_SIZE = 20;
    static final int FEED_BLOCK_SIZE = 5;
    static final int MAX_FIRST_PAGE_RECOMMENDATIONS = 4;

    private final IntUnaryOperator recommendationSlotPicker;

    PostFeedComposer() {
        this(ignored -> ThreadLocalRandom.current().nextInt(FEED_BLOCK_SIZE));
    }

    PostFeedComposer(IntUnaryOperator recommendationSlotPicker) {
        this.recommendationSlotPicker = recommendationSlotPicker;
    }

    int calculateRecommendationCount(int basicPostCount, int recommendationCandidateCount) {
        if (basicPostCount <= 0 || recommendationCandidateCount <= 0) {
            return 0;
        }

        return Math.min(
                Math.min(recommendationCandidateCount, MAX_FIRST_PAGE_RECOMMENDATIONS),
                basicPostCount / (FEED_BLOCK_SIZE - 1)
        );
    }

    List<PostResponse.FeedSummary> composeFirstPageFeed(List<PostResponse.FeedSummary> basicPosts,
                                                        List<PostResponse.FeedSummary> recommendations) {
        int recommendationCount = calculateRecommendationCount(basicPosts.size(), recommendations.size());
        if (recommendationCount == 0) {
            return basicPosts;
        }

        List<PostResponse.FeedSummary> feed = new ArrayList<>(basicPosts.size() + recommendationCount);
        int basicIndex = 0;

        for (int recommendationIndex = 0; recommendationIndex < recommendationCount; recommendationIndex++) {
            List<PostResponse.FeedSummary> block = new ArrayList<>(FEED_BLOCK_SIZE);
            for (int i = 0; i < FEED_BLOCK_SIZE - 1 && basicIndex < basicPosts.size(); i++) {
                block.add(basicPosts.get(basicIndex++));
            }

            block.add(recommendationSlotPicker.applyAsInt(recommendationIndex), recommendations.get(recommendationIndex));
            feed.addAll(block);
        }

        while (basicIndex < basicPosts.size() && feed.size() < POST_PAGE_SIZE) {
            feed.add(basicPosts.get(basicIndex++));
        }

        return feed;
    }
}
