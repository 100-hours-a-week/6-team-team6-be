package ktb.billage.fixture;

import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.user.User;

public final class MembershipFixture {

    private MembershipFixture() {}

    public static Membership one(Group group, User user) {
        return new Membership(
                group.getId(),
                user.getId(),
                "member"
        );
    }
}
