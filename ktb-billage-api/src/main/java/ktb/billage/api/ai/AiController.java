package ktb.billage.api.ai;

import jakarta.validation.constraints.NotNull;
import ktb.billage.application.ai.AiFacade;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiFacade aiFacade;

    @PostMapping(value = "/post-drafts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse.PostDraft> makePostDraftByAi(
            @RequestPart("images") @NotNull List<@NotNull MultipartFile> images
    ) {
//        return ResponseEntity.ok(aiFacade.makePostDraftByAi(images));
        return ResponseEntity.ok(new PostResponse.PostDraft("드릴 대여",
                "생활용 드릴입니다.",
                new BigDecimal("5000"),
                "DAY"));
    }
}
