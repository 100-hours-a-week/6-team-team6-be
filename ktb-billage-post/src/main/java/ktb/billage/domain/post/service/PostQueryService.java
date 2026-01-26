package ktb.billage.domain.post.service;

import ktb.billage.application.port.in.GroupPolicyFacade;
import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.post.PostImage;
import ktb.billage.domain.post.PostImageRepository;
import ktb.billage.domain.post.PostRepository;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    private final CursorCodec cursorCodec;
    private final GroupPolicyFacade groupPolicyFacade;

    public PostResponse.Summaries getPostsByCursor(Long groupId, Long userId, String cursor) {
        groupPolicyFacade.validateMembership(groupId, userId);

        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<Post> posts = loadPosts(decoded);
        boolean hasNextPage = posts.size() > 20;
        List<Post> pagePosts = hasNextPage ? posts.subList(0, 20) : posts;

        List<PostResponse.Summary> summaries = pagePosts.stream()
                .map(this::toSummary)
                .toList();

        String nextCursor = null;
        if (hasNextPage) {
            Post last = pagePosts.getLast();
            nextCursor = cursorCodec.encode(last.getCreatedAt(), last.getId());
        }

        return new PostResponse.Summaries(summaries, nextCursor, hasNextPage);
    }

    private CursorCodec.Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return cursorCodec.decode(cursor);
    }

    private List<Post> loadPosts(CursorCodec.Cursor decoded) {
        if (decoded == null) {
            return postRepository.findTop21ByOrderByUpdatedAtDescIdDesc();
        }

        return postRepository.findNextPage(decoded.time(), decoded.id(), PageRequest.of(0, 21));
    }

    private PostResponse.Summary toSummary(Post post) { // FIXME. 이미지 N + 1 문제 야기
        PostImage firstImage = postImageRepository
                .findFirstByPostIdAndDeletedAtIsNullOrderBySortOrderAsc(post.getId());

        return new PostResponse.Summary(
                post.getId(),
                post.getTitle(),
                firstImage.getId(),
                firstImage.getImageUrl(),
                post.getRentalFee(),
                post.getFeeUnit(),
                post.getRentalStatus()
        );
    }
}
