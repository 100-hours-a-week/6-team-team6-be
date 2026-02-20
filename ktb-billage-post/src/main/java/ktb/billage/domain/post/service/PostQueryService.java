package ktb.billage.domain.post.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.common.exception.PostException;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.post.PostImage;
import ktb.billage.domain.post.PostImageRepository;
import ktb.billage.domain.post.PostQueryRepository;
import ktb.billage.domain.post.PostRepository;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.IMAGE_NOT_FOUND;
import static ktb.billage.common.exception.ExceptionCode.POST_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    private final CursorCodec cursorCodec;
    private final PostQueryRepository postQueryRepository;

    public PostResponse.Summaries getPostsByCursor(Long groupId, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<Post> posts = loadPosts(groupId, decoded);
        return buildSummaries(posts);
    }

    public PostResponse.Summaries getPostsByKeywordAndCursor(Long groupId, String keyword, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<Post> posts = loadPostsByKeyword(groupId, keyword, decoded);
        return buildSummaries(posts);
    }

    public PostResponse.DetailCore getPostDetailCore(Long postId) {
        Post post = findPost(postId);
        List<PostResponse.ImageInfo> imageInfos = postQueryRepository.findImageInfos(postId);
        return new PostResponse.DetailCore(
                post.getTitle(),
                post.getContent(),
                new PostResponse.ImageUrls(imageInfos),
                post.getSellerId(),
                post.getRentalFee(),
                post.getFeeUnit(),
                post.getRentalStatus(),
                post.getUpdatedAt()
        );
    }

    public Long findGroupIdByPostId(Long postId){
        return postRepository.findGroupIdByPostId(postId);
    }

    public Long findSellerIdByPostId(Long postId) {
        return findPost(postId).getSellerId();
    }

    public void validatePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostException(POST_NOT_FOUND);
        }
    }

    public PostResponse.MySummaries getMyPostsByCursor(List<Long> membershipIds, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<PostResponse.MySummary> myPosts = loadMyPosts(membershipIds, decoded);
        return buildMySummaries(myPosts);
    }

    public String findPostFirstImageUrl(Long postId) {
        return postImageRepository.findFirstByPostIdAndDeletedAtIsNullOrderBySortOrderAsc(postId)
                .orElseThrow(() -> new PostException(IMAGE_NOT_FOUND))
                .getImageUrl();
    }

    private Post findPost(Long postId) {
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostException(POST_NOT_FOUND));
    }

    private CursorCodec.Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return cursorCodec.decode(cursor);
    }

    private List<PostResponse.MySummary> loadMyPosts(List<Long> membershipIds, CursorCodec.Cursor cursor) {
        if (cursor == null) {
            return postRepository.findTop21ByMyPosts(membershipIds, PageRequest.of(0, 21));
        }

        return postRepository.findNextMyPosts(membershipIds, cursor.time(), cursor.id(), PageRequest.of(0, 21));
    }

    private List<Post> loadPosts(Long groupId, CursorCodec.Cursor decoded) {
        if (decoded == null) {
            return postRepository.findTop21ByGroupIdOrderByUpdatedAtDescIdDesc(groupId, PageRequest.of(0, 21));
        }

        return postRepository.findNextPage(groupId, decoded.time(), decoded.id(), PageRequest.of(0, 21));
    }

    private List<Post> loadPostsByKeyword(Long groupId, String keyword, CursorCodec.Cursor decoded) {
        String fullTextKeyword = toBooleanModeKeyword(keyword);
        if (decoded == null) {
            return postRepository.findTop21ByGroupIdAndContainingKeywordOrderByUpdatedAtDescIdDesc(
                    groupId,
                    fullTextKeyword,
                    PageRequest.of(0, 21)
            );
        }

        return postRepository.findNextPageByKeyword(
                groupId,
                fullTextKeyword,
                decoded.time(),
                decoded.id(),
                PageRequest.of(0, 21)
        );
    }

    private String toBooleanModeKeyword(String keyword) {
        String normalized = keyword.trim().replaceAll("\\s+", " ");

        return "+\"" + normalized.replace("\"", "\\\"") + "\"";
    }

    private PostResponse.Summaries buildSummaries(List<Post> posts) {
        boolean hasNextPage = posts.size() > 20;
        List<Post> pagePosts = hasNextPage ? posts.subList(0, 20) : posts;

        List<PostResponse.Summary> summaries = pagePosts.stream()
                .map(this::toSummary)
                .toList();

        String nextCursor = null;
        if (hasNextPage) {
            Post last = pagePosts.getLast();
            nextCursor = cursorCodec.encode(last.getUpdatedAt(), last.getId());
        }

        return new PostResponse.Summaries(summaries, nextCursor, hasNextPage);
    }

    private PostResponse.MySummaries buildMySummaries(List<PostResponse.MySummary> posts) {
        boolean hasNextPage = posts.size() > 20;
        List<PostResponse.MySummary> pagePosts = hasNextPage ? posts.subList(0, 20) : posts;

        String nextCursor = null;
        if (hasNextPage) {
            PostResponse.MySummary last = pagePosts.getLast();
            nextCursor = cursorCodec.encode(last.updatedAt(), last.postId());
        }

        return new PostResponse.MySummaries(pagePosts, nextCursor, hasNextPage);
    }

    private PostResponse.Summary toSummary(Post post) { // FIXME. 이미지 N + 1 문제 야기
        PostImage firstImage = postImageRepository
                .findFirstByPostIdAndDeletedAtIsNullOrderBySortOrderAsc(post.getId())
                .orElseThrow(() -> new PostException(IMAGE_NOT_FOUND));

        return new PostResponse.Summary(
                post.getId(),
                post.getTitle(),
                firstImage.getId(),
                firstImage.getImageUrl(),
                post.getRentalFee(),
                post.getFeeUnit(),
                post.getRentalStatus(),
                post.getUpdatedAt()
        );
    }
}
