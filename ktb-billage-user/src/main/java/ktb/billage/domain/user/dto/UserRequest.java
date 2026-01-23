package ktb.billage.domain.user.dto;

import ktb.billage.domain.user.validation.LoginId;
import ktb.billage.domain.user.validation.Password;

public class UserRequest {

    public record Join(
            @LoginId String loginId,
            @Password String password) {
    }
}
