package ktb.billage.application.keywordsubscription;

import ktb.billage.application.post.event.PostCreateEvent;
import ktb.billage.application.post.event.PostUpsertPayload;
import ktb.billage.domain.keywordsubscription.KeywordSubscription;
import ktb.billage.domain.keywordsubscription.service.KeywordSubscriptionService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCreatedKeywordNotificationUseCaseUnitTest {
    @Mock
    private KeywordSubscriptionService keywordSubscriptionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private PostCreatedKeywordNotificationUseCase useCase;

    @Test
    @DisplayName("게시글 제목과 일치하는 키워드 구독이 없으면 알림을 저장하지 않는다")
    void handle_ShouldReturnWithoutSaving_WhenNoKeywordSubscriptionMatchesTitle() {
        PostCreateEvent event = createEvent(11L, 22L, 33L, "맥북 팝니다");
        Long postAuthorUserId = 101L;
        List<KeywordSubscription> activeSubscriptions = List.of(
                new KeywordSubscription(201L, 22L, "아이폰"),
                new KeywordSubscription(202L, 22L, "에어팟")
        );

        when(membershipService.findUserIdByMembershipId(11L)).thenReturn(postAuthorUserId);
        when(keywordSubscriptionService.findAllActiveSubscriptionsInGroupAndNotMine(22L, postAuthorUserId))
                .thenReturn(activeSubscriptions);
        when(keywordSubscriptionService.findMatchedKeywordSubscription(activeSubscriptions, "맥북 팝니다"))
                .thenReturn(List.of());

        useCase.handle(event);

        verify(membershipService).findUserIdByMembershipId(11L);
        verify(keywordSubscriptionService).findAllActiveSubscriptionsInGroupAndNotMine(22L, postAuthorUserId);
        verify(keywordSubscriptionService).findMatchedKeywordSubscription(activeSubscriptions, "맥북 팝니다");
        verify(notificationService, never()).saveAllNotifications(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("같은 유저의 중복 키워드 매칭은 하나로 합쳐서 POST 알림을 저장한다")
    void handle_ShouldSaveNotificationsForMatchedUsersOnlyOncePerUser() {
        PostCreateEvent event = createEvent(11L, 22L, 33L, "아이폰 에어팟 일괄 판매");
        Long postAuthorUserId = 101L;
        List<KeywordSubscription> activeSubscriptions = List.of(
                new KeywordSubscription(201L, 22L, "아이폰"),
                new KeywordSubscription(201L, 22L, "에어팟"),
                new KeywordSubscription(202L, 22L, "에어팟")
        );
        List<KeywordSubscription> matchedSubscriptions = activeSubscriptions;

        when(membershipService.findUserIdByMembershipId(11L)).thenReturn(postAuthorUserId);
        when(keywordSubscriptionService.findAllActiveSubscriptionsInGroupAndNotMine(22L, postAuthorUserId))
                .thenReturn(activeSubscriptions);
        when(keywordSubscriptionService.findMatchedKeywordSubscription(activeSubscriptions, "아이폰 에어팟 일괄 판매"))
                .thenReturn(matchedSubscriptions);

        useCase.handle(event);

        ArgumentCaptor<List<Notification>> notificationsCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).saveAllNotifications(notificationsCaptor.capture());

        List<Notification> notifications = notificationsCaptor.getValue();
        assertThat(notifications).hasSize(2);
        assertThat(notifications)
                .extracting(Notification::getUserId)
                .containsExactlyInAnyOrder(201L, 202L);
        assertThat(notifications)
                .allSatisfy(notification -> assertThat(notification.getChatroomId()).isNull());
        assertThat(notifications)
                .allSatisfy(notification -> assertThat(notification.getPostId()).isEqualTo(33L));
    }

    private PostCreateEvent createEvent(Long membershipId, Long groupId, Long postId, String title) {
        return new PostCreateEvent(new PostUpsertPayload(
                membershipId,
                groupId,
                postId,
                "image-url",
                title,
                BigDecimal.valueOf(1000),
                "MONTH"
        ));
    }
}
