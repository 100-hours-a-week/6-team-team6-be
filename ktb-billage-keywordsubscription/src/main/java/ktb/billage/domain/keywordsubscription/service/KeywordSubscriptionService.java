package ktb.billage.domain.keywordsubscription.service;

import ktb.billage.common.exception.KeywordSubscriptionException;
import ktb.billage.domain.keywordsubscription.KeywordSubscription;
import ktb.billage.domain.keywordsubscription.KeywordSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_ALREADY_EXISTS;
import static ktb.billage.common.exception.ExceptionCode.KEYWORD_SUBSCRIPTION_EXCEED_LIMIT;

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
}
