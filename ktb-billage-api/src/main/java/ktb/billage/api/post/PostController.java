package ktb.billage.api.post;

import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.service.PostService;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/groups/{groupId}/posts")
    ResponseEntity<?> createPost(@PathVariable Long groupId,
                                 @RequestBody PostRequest.Create request,
                                 @AuthenticatedId Long userId) {
        return ResponseEntity.status(CREATED)
                .body(postService.create(groupId, userId, request.title(),
                        request.content(), request.imageUrls(), request.rentalFee(), request.feeUnit()));
    }
}
