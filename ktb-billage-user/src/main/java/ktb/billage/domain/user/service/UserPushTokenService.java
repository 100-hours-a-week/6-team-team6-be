package ktb.billage.domain.user.service;

import ktb.billage.domain.user.UserPushToken;
import ktb.billage.domain.user.UserPushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPushTokenService {
    private final UserPushTokenRepository userPushTokenRepository;

    @Transactional(readOnly = true)
    public List<String> findTokensByUserId(Long userId) {
        return userPushTokenRepository.findAllByUserId(userId).stream()
                .map(UserPushToken::getFcmToken)
                .toList();
    }

    @Transactional
    public void deleteInvalidTokens(List<String> fcmTokens) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }
        userPushTokenRepository.deleteByFcmTokenIn(fcmTokens);
    }
}
