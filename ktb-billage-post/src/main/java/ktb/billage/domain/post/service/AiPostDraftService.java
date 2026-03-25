package ktb.billage.domain.post.service;

import ktb.billage.domain.post.ai.AiPostDraftClient;
import ktb.billage.domain.post.dto.PostDraft;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPostDraftService {
    private final AiPostDraftClient aiPostDraftClient;

    public PostResponse.PostDraft makePostDraft(List<PostRequest.ImageComponent> images) {
        PostDraft response =  aiPostDraftClient.requestPostDraft(images);
        return new PostResponse.PostDraft(
                response.title(),
                response.content(),
                response.price() == 0 ? BigDecimal.valueOf(0) : BigDecimal.valueOf(response.price()),
                "HOUR",
                response.isRentable()
        );
    }
}
