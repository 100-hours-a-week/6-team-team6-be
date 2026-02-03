package ktb.billage.api.post;

import jakarta.validation.Valid;
import ktb.billage.application.post.PostFacade;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostFacade postFacade;

    @PostMapping("/groups/{groupId}/posts")
    public ResponseEntity<PostResponse.Id> createPost(@PathVariable Long groupId,
                                                      @Valid @RequestBody PostRequest.Create request,
                                                      @AuthenticatedId Long userId) {
        return ResponseEntity.status(CREATED)
                .body(postFacade.create(groupId, userId, request));
    }

    @PutMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<PostResponse.Id> modifyPost(@PathVariable Long groupId, @PathVariable Long postId,
                                                      @Valid @RequestBody PostRequest.Update request, @AuthenticatedId Long userId) {
        return ResponseEntity.ok()
                .body(postFacade.update(groupId, postId, userId, request));
    }

    @PatchMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<PostResponse.ChangedStatus> changeRentalStatus(@PathVariable Long groupId, @PathVariable Long postId,
                                                                         @RequestBody PostRequest.Change request, @AuthenticatedId Long userId) {
        return ResponseEntity.ok()
                .body(postFacade.changeRentalStatus(groupId, postId, userId, request.status()));
    }

    @DeleteMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long groupId, @PathVariable Long postId, @AuthenticatedId Long userId) {
        postFacade.delete(groupId, postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/posts")
    public ResponseEntity<PostResponse.Summaries> getPostsByKeywordAndCursor(@PathVariable Long groupId, @AuthenticatedId Long userId,
                                                                             @RequestParam(required = false) String query, @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok()
                .body(
                        query == null ? postFacade.getPostsByCursor(groupId, userId, cursor)
                        : postFacade.getPostsByKeywordAndCursor(groupId, userId, query, cursor)
                );
    }

    @GetMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long groupId, @PathVariable Long postId, @AuthenticatedId Long userId) {
        return ResponseEntity.ok()
                .body(postFacade.getPostDetail(groupId, postId, userId));
    }
}
