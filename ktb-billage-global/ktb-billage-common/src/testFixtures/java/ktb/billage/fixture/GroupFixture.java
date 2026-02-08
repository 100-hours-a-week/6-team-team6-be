package ktb.billage.fixture;

import ktb.billage.domain.group.Group;

public final class GroupFixture {

    private GroupFixture() {}

    public static Group one(String groupName) {
        return new Group(
                groupName == null ? "test Group" : groupName,
                "dummy.cover"
        );
    }
}
