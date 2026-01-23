package ktb.billage.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.domain.entity.BaseEntity;
import ktb.billage.exception.AuthException;
import ktb.billage.exception.InternalException;
import ktb.billage.exception.UserException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static ktb.billage.exception.ExceptionCode.AUTHENTICATION_FAILED;
import static ktb.billage.exception.ExceptionCode.INVALID_LOGIN_ID;
import static ktb.billage.exception.ExceptionCode.INVALID_NICKNAME;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,16}$");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]{6}$");

    private static final String DEFAULT_AVATAR_URL = "temp.url";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String loginId;

    private String password;

    private String nickname;

    private String avatarUrl;

    public User(String loginId, String encodedPassword, String nickname) {
        validateLoginId(loginId);
        validateNickname(nickname);
        this.loginId = loginId;
        this.password = encodedPassword; // TODO. 비밀번호 값에 대한 도메인 검증 방식 고민 필요
        this.nickname = nickname;
        this.avatarUrl = DEFAULT_AVATAR_URL;
    }

    public void verifyPassword(PasswordEncoder passwordEncoder, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, this.password)) {
            throw new AuthException(AUTHENTICATION_FAILED);
        }
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new UserException(INVALID_LOGIN_ID);
        }

        if (!LOGIN_ID_PATTERN.matcher(loginId).matches()) {
            throw new UserException(INVALID_LOGIN_ID);
        }
    }

    private void validateNickname(String nickname) throws InternalException {
        if (nickname == null || nickname.isBlank()) {
            throw new InternalException(INVALID_NICKNAME); // 닉네임 생성은 서버 내부 로직이기에 InternalExcpetion으로 던짐
        }

        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new InternalException(INVALID_NICKNAME);
        }
    }
}
