package ktb.billage.application.keywordsubscription;

import ktb.billage.application.post.event.PostCreateEvent;
import ktb.billage.application.post.event.PostUpsertPayload;
import ktb.billage.domain.keywordsubscription.KeywordSubscription;
import ktb.billage.domain.keywordsubscription.service.KeywordSubscriptionService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.notification.Type;
import ktb.billage.domain.notification.service.NotificationService;
import ktb.billage.domain.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCreatedKeywordNotificationUseCase {
    private final KeywordSubscriptionService keywordSubscriptionService;
    private final NotificationService notificationService;
    private final MembershipService membershipService;

    public void handle(PostCreateEvent event) {
        PostUpsertPayload payload = event.payload();

        Long postAuthorUserId = membershipService.findUserIdByMembershipId(payload.membershipId());
        List<KeywordSubscription> activeKeywordSubscriptions = keywordSubscriptionService.findAllActiveSubscriptionsInGroupAndNotMine(payload.groupId(), postAuthorUserId);

        List<KeywordSubscription> matchedKeywordSubscription = keywordSubscriptionService.findMatchedKeywordSubscription(activeKeywordSubscriptions, payload.title());
        if (matchedKeywordSubscription.isEmpty()) {
            return;
        }

        Map<Long, KeywordSubscription> deduplicatedSubscriptionsByUserId = deduplicateKeywordSubscriptionByUserId(matchedKeywordSubscription);

        List<Notification> notificationsToSave = createNotificationsToSave(deduplicatedSubscriptionsByUserId, payload.postId(), payload.title());

        notificationService.saveAllNotifications(notificationsToSave);
    }
    
    private Map<Long, KeywordSubscription> deduplicateKeywordSubscriptionByUserId(List<KeywordSubscription> keywordSubscriptions) {
        return keywordSubscriptions.stream()
                .collect(Collectors.toMap(
                        KeywordSubscription::getUserId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private List<Notification> createNotificationsToSave(Map<Long, KeywordSubscription> deduplicatedSubscriptionsByUserId, Long postId, String title) {
        return deduplicatedSubscriptionsByUserId.values().stream()
                .map(subscription -> new Notification(
                        subscription.getUserId(),
                        subscription.getGroupId(),
                        postId,
                        null,
                        subscription.getKeyword(),
                        title,
                        Type.POST
                ))
                .toList();
    }
}
