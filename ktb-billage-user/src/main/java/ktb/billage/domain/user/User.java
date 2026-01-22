package ktb.billage.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.domain.entity.BaseEntity;
import ktb.billage.exception.AuthException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static ktb.billage.exception.ExceptionCode.AUTHENTICATION_FAILED;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String loginId;

    private String password;

    private String avatarUrl;

    public User(String loginId, String encodedPassword) {
        this.loginId = loginId;
        this.password = encodedPassword;
        this.avatarUrl = "temp.url";
    }

    public void verifyPassword(PasswordEncoder passwordEncoder, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, this.password)) {
            throw new AuthException(AUTHENTICATION_FAILED);
        }
    }
}
