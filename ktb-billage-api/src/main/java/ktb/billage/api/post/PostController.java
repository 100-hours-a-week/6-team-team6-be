package ktb.billage.api.post;

import jakarta.validation.Valid;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.service.PostService;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/groups/{groupId}/posts")
    ResponseEntity<PostResponse.Id> createPost(@PathVariable Long groupId,
                                               @Valid @RequestBody PostRequest.Create request,
                                               @AuthenticatedId Long userId) {

        return ResponseEntity.status(CREATED)
                .body(postService.create(groupId, userId, request.title(),
                        request.content(), request.imageUrls(), request.rentalFee(), request.feeUnit()));
    }

    @PutMapping("/groups/{groupId}/posts/{postId}")
    ResponseEntity<PostResponse.Id> modifyPost(@PathVariable Long groupId, @PathVariable Long postId,
                                               @Valid @RequestBody PostRequest.Update request, @AuthenticatedId Long userId) {

        return ResponseEntity.ok()
                .body(postService.update(groupId, postId, userId, request.title(),
                        request.content(), request.imageUrls(), request.rentalFee(), request.feeUnit()));
    }

    @PatchMapping("/groups/{groupId}/posts/{postId}")
    ResponseEntity<PostResponse.ChangedStatus> changeRentalStatus(@PathVariable Long groupId, @PathVariable Long postId,
                                                                  @RequestBody PostRequest.Change request, @AuthenticatedId Long userId) {

        return ResponseEntity.ok()
                .body(postService.changeRentalStatus(groupId, postId, userId, request.rentalStatus()));
    }

    @DeleteMapping("/groups/{groupId}/posts/{postId}")
    ResponseEntity<Void> deletePost(@PathVariable Long groupId, @PathVariable Long postId, @AuthenticatedId Long userId) {
        postService.delete(groupId, postId, userId);
        return ResponseEntity.noContent().build();
    }
}
