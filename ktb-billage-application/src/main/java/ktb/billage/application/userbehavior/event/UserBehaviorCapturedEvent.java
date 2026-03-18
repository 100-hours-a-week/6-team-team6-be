package ktb.billage.application.userbehavior.event;

import ktb.billage.domain.post.userbehavior.UserBehaviorType;

public record UserBehaviorCapturedEvent(
        Long membershipId,
        Long groupId,
        UserBehaviorType type,
        String content
) {
}
