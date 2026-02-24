package ktb.billage.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ktb.billage.domain.user.UserPushToken;
import ktb.billage.domain.user.validation.LoginId;
import ktb.billage.domain.user.validation.Password;

public class UserRequest {

    public record Join(
            @LoginId String loginId,
            @Password String password) {
    }

    public record WebPushEnabled(boolean enabled) {
    }

    public record PushToken(
            @NotNull UserPushToken.PushPlatform platform,
            @NotBlank String deviceId,
            @NotBlank String newToken
    ) {
    }
}
