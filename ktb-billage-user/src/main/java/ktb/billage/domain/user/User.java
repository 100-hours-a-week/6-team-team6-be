package ktb.billage.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.common.entity.BaseEntity;
import ktb.billage.common.exception.AuthException;
import ktb.billage.common.exception.InternalException;
import ktb.billage.common.exception.UserException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static ktb.billage.common.exception.ExceptionCode.AUTHENTICATION_FAILED;
import static ktb.billage.common.exception.ExceptionCode.INVALID_LOGIN_ID;
import static ktb.billage.common.exception.ExceptionCode.INVALID_NICKNAME;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,16}$");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]{6}$");

    private static final String DEFAULT_AVATAR_URL = "images/default-avatar.png";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

    public User(String loginId, String encodedPassword, String nickname) {
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
}
