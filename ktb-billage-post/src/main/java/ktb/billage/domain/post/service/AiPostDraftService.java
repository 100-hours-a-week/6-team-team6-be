package ktb.billage.domain.post.service;

import ktb.billage.domain.post.ai.AiPostDraftClient;
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
        var response =  aiPostDraftClient.requestPostDraft(images);
        return new PostResponse.PostDraft(
                response.title(),
                response.content(),
                response.rentalFee() == null ? BigDecimal.valueOf(0) : response.rentalFee(),
                response.feeUnit() == null ? "HOUR" : response.feeUnit()
        );
    }
}
