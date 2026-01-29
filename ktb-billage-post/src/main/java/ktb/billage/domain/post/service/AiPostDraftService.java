package ktb.billage.domain.post.service;

import ktb.billage.domain.post.ai.AiPostDraftClient;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPostDraftService {
    private final AiPostDraftClient aiPostDraftClient;

    public PostResponse.PostDraft makePostDraft(List<PostRequest.ImageComponent> images) {
        return aiPostDraftClient.requestPostDraft(images);
    }
}
