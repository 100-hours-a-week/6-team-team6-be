package ktb.billage.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPushTokenRepository extends JpaRepository<UserPushToken, Long> {

    Optional<UserPushToken> findByUserIdAndPlatformAndDeviceId(Long userId, UserPushToken.PushPlatform platform, String deviceId);

    List<UserPushToken> findAllByUserId(Long userId);

    void deleteByFcmTokenIn(List<String> fcmTokens);
}
