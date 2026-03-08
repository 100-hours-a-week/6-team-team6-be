package ktb.billage.application.keywordsubscription;

import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.keywordsubscription.dto.KeywordSubscriptionResponse;
import ktb.billage.domain.keywordsubscription.service.KeywordSubscriptionService;
import ktb.billage.domain.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class KeywordSubscriptionFacade {
    private final KeywordSubscriptionService keywordSubscriptionService;
    private final GroupService groupService;
    private final MembershipService membershipService;

    public KeywordSubscriptionResponse.Id registerKeyword(Long userId, Long groupId, String keyword) {
        groupService.validateGroup(groupId);
        membershipService.validateMembership(groupId, userId);

        Long subscriptionId = keywordSubscriptionService.register(userId, groupId, keyword);

        return new KeywordSubscriptionResponse.Id(subscriptionId);
    }

    public void delete(Long userId, Long groupId, Long keywordSubscriptionId) {
        groupService.validateGroup(groupId);
        membershipService.validateMembership(groupId, userId);

        keywordSubscriptionService.softDelete(userId, keywordSubscriptionId);
    }

    public KeywordSubscriptionResponse.Summaries getMyKeywordSubscriptionsInGroup(Long userId, Long groupId) {
        groupService.validateGroup(groupId);
        membershipService.validateMembership(groupId, userId);

        return keywordSubscriptionService.getKeywordSubscritpions(userId, groupId);
    }
}
