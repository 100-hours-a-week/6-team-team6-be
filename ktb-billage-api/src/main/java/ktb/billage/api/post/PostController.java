package ktb.billage.api.post;

import jakarta.validation.Valid;
import ktb.billage.application.chat.ChatFacade;
import ktb.billage.application.group.GroupFacade;
import ktb.billage.application.post.PostFacade;
import ktb.billage.application.user.UserFacade;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.service.PostQueryService;
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
    private final GroupFacade groupFacade;
    private final PostFacade postFacade;
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final PostQueryService postQueryService;

    @PostMapping("/groups/{groupId}/posts")
    public ResponseEntity<PostResponse.Id> createPost(@PathVariable Long groupId,
                                                      @Valid @RequestBody PostRequest.Create request,
                                                      @AuthenticatedId Long userId) {
        Long membershipId = groupFacade.requireMembershipIdForAccess(groupId, userId);

        return ResponseEntity.status(CREATED)
                .body(postFacade.create(membershipId, request));
    }

    @PutMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<PostResponse.Id> modifyPost(@PathVariable Long groupId, @PathVariable Long postId,
                                                      @Valid @RequestBody PostRequest.Update request, @AuthenticatedId Long userId) {
        Long membershipId = groupFacade.requireMembershipIdForAccess(groupId, userId);

        return ResponseEntity.ok()
                .body(postFacade.update(postId, membershipId, request));
    }

    @PatchMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<PostResponse.ChangedStatus> changeRentalStatus(@PathVariable Long groupId, @PathVariable Long postId,
                                                                         @RequestBody PostRequest.Change request, @AuthenticatedId Long userId) {
        Long membershipId = groupFacade.requireMembershipIdForAccess(groupId, userId);

        return ResponseEntity.ok()
                .body(postFacade.changeRentalStatus(postId, membershipId, request.rentalStatus()));
    }

    @DeleteMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long groupId, @PathVariable Long postId, @AuthenticatedId Long userId) {
        Long membershipId = groupFacade.requireMembershipIdForAccess(groupId, userId);
        postFacade.delete(postId, membershipId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/posts")
    public ResponseEntity<PostResponse.Summaries> getPostsByKeywordAndCursor(@PathVariable Long groupId, @AuthenticatedId Long userId,
                                                                             @RequestParam(required = false) String keyword, @RequestParam(required = false) String cursor) {
        groupFacade.validateMembership(groupId, userId);

        return ResponseEntity.ok()
                .body(
                        keyword == null ? postQueryService.getPostsByCursor(cursor)
                        : postQueryService.getPostsByKeywordAndCursor(keyword, cursor)
                );
    }

    @GetMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long groupId, @PathVariable Long postId, @AuthenticatedId Long userId) {
        Long membershipId = groupFacade.requireMembershipIdForAccess(groupId, userId);
        PostResponse.DetailCore core = postQueryService.getPostDetailCore(postId);
        boolean isSeller = core.sellerId().equals(membershipId);

        Long sellerUserId = groupFacade.findUserIdByMembershipId(core.sellerId());
        UserFacade.UserSummary sellerSummary = userFacade.getUserSummary(sellerUserId);

        Long chatroomId = isSeller
                ? -1L
                : chatFacade.findChatroomIdByPostIdAndBuyerId(postId, membershipId);
        Long activeChatroomCount = isSeller
                ? chatFacade.countChatroomsByPostId(postId)
                : -1L;

        return ResponseEntity.ok()
                .body(new PostResponse.Detail(
                        core.title(),
                        core.content(),
                        core.imageUrls(),
                        core.sellerId(),
                        sellerSummary.nickname(),
                        sellerSummary.avatarUrl(),
                        core.rentalFee(),
                        core.feeUnit(),
                        core.rentalStatus(),
                        core.updatedAt(),
                        isSeller,
                        chatroomId,
                        activeChatroomCount
                ));
    }
}
