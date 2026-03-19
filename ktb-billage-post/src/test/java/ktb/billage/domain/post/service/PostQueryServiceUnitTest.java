package ktb.billage.domain.post.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.common.image.ImageService;
import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.post.PostImage;
import ktb.billage.domain.post.PostImageRepository;
import ktb.billage.domain.post.PostQueryRepository;
import ktb.billage.domain.post.PostRepository;
import ktb.billage.domain.post.dto.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceUnitTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private PostQueryService postQueryService;

    @Test
    @DisplayName("추천 게시글 응답은 AI가 준 게시글 id 순서를 유지하고 존재하지 않는 게시글은 제외한다")
    void getRecommendations_ordersByAiSequenceAndSkipsUnavailablePosts() {
        Post first = new Post(1L, "first", "content", BigDecimal.valueOf(1000), FeeUnit.HOUR, 1);
        Post second = new Post(1L, "second", "content", BigDecimal.valueOf(2000), FeeUnit.DAY, 1);
        setId(first, 104L);
        setId(second, 105L);

        given(postRepository.findAllByIdInAndDeletedAtIsNull(anyList()))
                .willReturn(List.of(second, first));
        given(postImageRepository.findAllFirstImagesByPostIds(anyList()))
                .willReturn(List.of(
                        new PostImage(first, "img-104", 1),
                        new PostImage(second, "img-105", 1)
                ));
        given(imageService.resolveUrl("img-104")).willReturn("resolved-img-104");
        given(imageService.resolveUrl("img-105")).willReturn("resolved-img-105");

        PostResponse.Recommendations response = postQueryService.getRecommendations(List.of(104L, 999L, 105L));

        assertThat(response.size()).isEqualTo(2);
        assertThat(response.recommendations())
                .extracting(PostResponse.Recommendation::postId)
                .containsExactly(104L, 105L);
        assertThat(response.recommendations())
                .extracting(PostResponse.Recommendation::postFirstImageUrl)
                .containsExactly("resolved-img-104", "resolved-img-105");
    }

    private void setId(Post post, Long id) {
        try {
            var field = Post.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(post, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
