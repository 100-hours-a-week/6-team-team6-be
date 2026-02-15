package ktb.billage.domain.user;

import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.common.exception.AuthException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    @Nested
    class 비밀번호_일치_검증_테스트 {
        @Test
        void 일치_성공_예외_던지지_않음() {
            User user = new User("test1234", "encoded", "nick12");

            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
            when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

            assertThatCode(() -> user.verifyPassword(passwordEncoder, "raw"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 일치_실패_401_예외_던짐() {
            User user = new User("test1234", "encoded", "nick12");

            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
            when(passwordEncoder.matches("raw", "encoded")).thenReturn(false);

            assertThatThrownBy(() -> user.verifyPassword(passwordEncoder, "raw"))
                    .isInstanceOf(AuthException.class);
        }
    }
}
