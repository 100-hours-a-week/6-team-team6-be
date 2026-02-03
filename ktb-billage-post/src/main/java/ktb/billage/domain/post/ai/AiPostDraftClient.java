package ktb.billage.domain.post.ai;

import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;

import java.util.List;

public interface AiPostDraftClient {
    PostResponse.PostDraft requestPostDraft(List<PostRequest.ImageComponent> images);
}
