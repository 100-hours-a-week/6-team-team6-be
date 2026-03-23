package ktb.billage.domain.keywordsubscription.service;

import ktb.billage.common.exception.KeywordSubscriptionException;
import ktb.billage.domain.keywordsubscription.KeywordSubscription;
import ktb.billage.domain.keywordsubscription.KeywordSubscriptionRepository;
import ktb.billage.domain.keywordsubscription.dto.KeywordSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_ALREADY_DELETE;
import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_ALREADY_EXISTS;
import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_EXCEED_LIMIT;
import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_NOT_FOUND;
import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_NOT_OWNED;

@Service
@RequiredArgsConstructor
public class KeywordSubscriptionService {
    private static final int MAX_KEYWORD_SUBSCRIPTION_COUNT = 30;

    private final KeywordSubscriptionRepository keywordSubscriptionRepository;

    public Long register(Long userId, Long groupId, String keyword) {
        if (keywordSubscriptionRepository.countByUserIdAndGroupIdAndDeletedAtIsNull(userId, groupId) >= MAX_KEYWORD_SUBSCRIPTION_COUNT) {
            throw new KeywordSubscriptionException(KEYWORD_SUBSCRIPTION_EXCEED_LIMIT);
        }

        if (keywordSubscriptionRepository.existsByUserIdAndGroupIdAndKeywordAndDeletedAtIsNull(userId, groupId, keyword)) {
            throw new KeywordSubscriptionException(KEYWORD_SUBSCRIPTION_ALREADY_EXISTS);
        }

        return keywordSubscriptionRepository.save(new KeywordSubscription(userId, groupId, keyword)).getId();
    }

    public void softDelete(Long userId, Long keywordSubscriptionId) {
        var subscription = findKeywordSubscription(keywordSubscriptionId);

        if (!subscription.isOwned(userId)) {
            throw new KeywordSubscriptionException(KEYWORD_SUBSCRIPTION_NOT_OWNED);
        }

        if (subscription.isDeleted()) {
            throw new KeywordSubscriptionException(KEYWORD_SUBSCRIPTION_ALREADY_DELETE);
        }

        subscription.delete(Instant.now());
    }

    private KeywordSubscription findKeywordSubscription(Long keywordSubscriptionId) {
        return keywordSubscriptionRepository.findById(keywordSubscriptionId)
                .orElseThrow(() -> new KeywordSubscriptionException(KEYWORD_SUBSCRIPTION_NOT_FOUND));
    }

    public KeywordSubscriptionResponse.Summaries getKeywordSubscritpions(Long userId, Long groupId) {
        List<KeywordSubscription> keywordSubscriptions = keywordSubscriptionRepository.findByUserIdAndGroupIdAndDeletedAtIsNull(userId, groupId);

        return new KeywordSubscriptionResponse.Summaries(
                keywordSubscriptions.stream()
                        .map(subscription -> new KeywordSubscriptionResponse.Summary(
                                subscription.getId(),
                                subscription.getKeyword(),
                                subscription.getCreatedAt()
                        )).toList()
        );
    }

    public void deleteAllByGroupIdAndUserId(Long groupId, Long userId) {
        Instant now = Instant.now();
        keywordSubscriptionRepository.softDeleteAllByGroupIdAndUserId(groupId, userId, now);
    }

    public List<KeywordSubscription> findAllActiveSubscriptionsInGroupAndNotMine(Long groupId, Long postAuthorUserId) {
        return keywordSubscriptionRepository.findAllByGroupIdAndNotUserIdAndDeletedAtIsNull(groupId, postAuthorUserId);
    }

    // TODO. 성능 저하 시 Trie 방식으로 개선 필요
    public List<KeywordSubscription> findMatchedKeywordSubscription(List<KeywordSubscription> subscriptions, String title) {
        return subscriptions.stream()
                .filter(subscription -> title.contains(subscription.getKeyword()))
                .toList();
    }
}
