package ktb.billage.fixture;

import ktb.billage.domain.user.User;

public final class UserFixture {


    private UserFixture() {}

    public static User one(String loginId, String password) {
        return new User(loginId == null ? "testlogin" : loginId,
                password == null ? "qwer1234Q!" : password
        );
    }
}
