package ktb.billage.fixture;

import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserPushToken;

import java.time.Instant;

public final class UserPushTokenFixture {
    private UserPushTokenFixture() {}

    public static UserPushToken one(
            User user,
            UserPushToken.PushPlatform platform,
            String deviceToken,
            String fcmToken) {
        return new UserPushToken(
                user, platform, deviceToken, fcmToken, Instant.now()
        );
    }
}
