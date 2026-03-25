package ktb.billage.domain.post.ai;

import ktb.billage.domain.post.dto.PostDraft;
import ktb.billage.domain.post.dto.PostRequest;

import java.util.List;

public interface AiPostDraftClient {
    PostDraft requestPostDraft(List<PostRequest.ImageComponent> images);
}
