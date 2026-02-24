package ktb.billage.api.ai;

import jakarta.validation.constraints.NotNull;
import ktb.billage.apidoc.AiApiDoc;
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

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController implements AiApiDoc {
    private final AiFacade aiFacade;

    @PostMapping(value = "/post-drafts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse.PostDraft> makePostDraftByAi(
            @RequestPart("image") @NotNull List<@NotNull MultipartFile> image
    ) {
        return ResponseEntity.ok(aiFacade.makePostDraftByAi(image));
    }
}
