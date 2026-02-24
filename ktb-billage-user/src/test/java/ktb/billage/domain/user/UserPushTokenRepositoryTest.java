package ktb.billage.domain.user;

import jakarta.persistence.EntityManager;
import ktb.billage.domain.UserJpaTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = UserJpaTestApplication.class)
class UserPushTokenRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPushTokenRepository userPushTokenRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void 푸시_토큰_조회_테스트_findByUserIdAndDeviceId() {
        User user = userRepository.save(new User("test", "encoded"));

        UserPushToken token = new UserPushToken(user, UserPushToken.PushPlatform.WEB, "testDeviceId", "token-blah", Instant.now());
        userPushTokenRepository.save(token);

        entityManager.flush();
        entityManager.clear();

        Optional<UserPushToken> optToken = userPushTokenRepository.findByUserIdAndDeviceId(user.getId(), token.getDeviceId());

        assertThat(optToken).isNotEmpty();
        UserPushToken actualToken = optToken.get();
        assertThat(actualToken.getUser().getId()).isEqualTo(user.getId());
        assertThat(actualToken.getDeviceId()).isEqualTo(token.getDeviceId());
    }
}
