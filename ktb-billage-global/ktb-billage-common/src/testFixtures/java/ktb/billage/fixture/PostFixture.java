package ktb.billage.fixture;

import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.Post;

import java.math.BigDecimal;

public final class PostFixture {

    private PostFixture() {}

    public static Post one(Membership membership) {
        return new Post(membership.getId(),
                "dummy title",
                "dummy content",
                BigDecimal.valueOf(1000),
                FeeUnit.HOUR,
                1
        );
    }
}
