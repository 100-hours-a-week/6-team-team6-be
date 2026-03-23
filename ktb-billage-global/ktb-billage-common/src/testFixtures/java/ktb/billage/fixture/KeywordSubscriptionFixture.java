package ktb.billage.fixture;

import ktb.billage.domain.group.Group;
import ktb.billage.domain.keywordsubscription.KeywordSubscription;
import ktb.billage.domain.user.User;

public final class KeywordSubscriptionFixture {

    private KeywordSubscriptionFixture() {}

    public static KeywordSubscription one(User user, Group group, String keyword) {
        return new KeywordSubscription(
                user.getId(), group.getId(), keyword
        );
    }
}
