package ktb.billage.domain.post.ai;

import java.util.List;

public interface AiPostRecommendationClient {

    List<Long> recommend(Long postId);
}
