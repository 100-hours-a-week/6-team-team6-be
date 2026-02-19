package ktb.billage.fixture;

import ktb.billage.domain.group.Group;

public final class GroupFixture {
    private static final String GROUP_DEFAULT_COVER = "/group-cover-images";

    private GroupFixture() {}

    public static Group one(String groupName) {
        return new Group(
                groupName == null ? "test Group" : groupName,
                "dummy.cover"
        );
    }

    public static Group defaultCover(String imageCover) {

        return new Group("default", GROUP_DEFAULT_COVER + imageCover);
    }
}
