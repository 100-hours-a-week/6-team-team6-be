package ktb.billage.application.post;

import ktb.billage.common.image.ImageService;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.post.ai.AiPostRecommendationClient;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.service.PostQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostFacadeUnitTest {
    @Mock
    private PostQueryService postQueryService;

    @Mock
    private ImageService imageService;

    @Mock
    private GroupService groupService;

    @Mock
    private MembershipService membershipService;

    @Mock
    private AiPostRecommendationClient aiPostRecommendationClient;

    @Test
    @DisplayName("그룹 게시글이 10개 미만이면 추천 게시글 조회를 수행하지 않는다")
    void getPostsByCursor_doesNotRequestRecommendations_whenActivePostCountIsLessThanTen() {
        Long groupId = 1L;
        Long userId = 2L;
        Long membershipId = 3L;
        PostResponse.FeedSummary summary = new PostResponse.FeedSummary(
                10L,
                "basic",
                100L,
                "img-1",
                BigDecimal.valueOf(1000),
                ktb.billage.domain.post.FeeUnit.HOUR,
                ktb.billage.domain.post.RentalStatus.AVAILABLE,
                Instant.parse("2026-03-19T00:00:00Z"),
                PostResponse.FeedItemType.BASIC
        );

        given(membershipService.findMembershipId(groupId, userId)).willReturn(membershipId);
        given(postQueryService.countActivePostsByGroupId(groupId)).willReturn(9L);
        given(postQueryService.getPostsByCursor(groupId, null))
                .willReturn(new PostResponse.Summaries(List.of(summary), null, false));
        given(imageService.resolveUrl("img-1")).willReturn("resolved-img-1");

        PostFacade postFacade = new PostFacade(
                null,
                postQueryService,
                imageService,
                groupService,
                membershipService,
                null,
                null,
                null,
                aiPostRecommendationClient,
                null,
                null,
                null
        );

        postFacade.getPostsByCursor(groupId, userId, null);

        verify(aiPostRecommendationClient, never()).recommendNeeds(membershipId);
    }

    @Test
    @DisplayName("첫 페이지 추천 비활성화 시 추천 조회를 수행하지 않는다")
    void getPostsByCursor_doesNotRequestRecommendations_whenFirstPageRecommendationDisabled() {
        Long groupId = 1L;
        Long userId = 2L;
        Long membershipId = 3L;
        PostResponse.FeedSummary summary = new PostResponse.FeedSummary(
                10L,
                "basic",
                100L,
                "img-1",
                BigDecimal.valueOf(1000),
                ktb.billage.domain.post.FeeUnit.HOUR,
                ktb.billage.domain.post.RentalStatus.AVAILABLE,
                Instant.parse("2026-03-19T00:00:00Z"),
                PostResponse.FeedItemType.BASIC
        );

        given(membershipService.findMembershipId(groupId, userId)).willReturn(membershipId);
        given(postQueryService.getPostsByCursor(groupId, null))
                .willReturn(new PostResponse.Summaries(List.of(summary), null, false));
        given(imageService.resolveUrl("img-1")).willReturn("resolved-img-1");

        PostFacade postFacade = new PostFacade(
                null,
                postQueryService,
                imageService,
                groupService,
                membershipService,
                null,
                null,
                null,
                aiPostRecommendationClient,
                null,
                null,
                null
        );
        ReflectionTestUtils.setField(postFacade, "firstPageRecommendationEnabled", false);

        postFacade.getPostsByCursor(groupId, userId, null);

        verify(postQueryService, never()).countActivePostsByGroupId(groupId);
        verify(aiPostRecommendationClient, never()).recommendNeeds(membershipId);
    }
}
