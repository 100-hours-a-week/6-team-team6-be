package ktb.billage.domain.user.service;

import ktb.billage.common.exception.UserException;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserPushToken;
import ktb.billage.domain.user.UserPushTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserPushTokenRepository userPushTokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("푸시토큰_삭제_테스트_성공")
    void deletePushToken_success() {
        Long userId = 1L;
        User user = new User("test", "encoded");
        UserPushToken testToken = new UserPushToken(user, UserPushToken.PushPlatform.WEB, "testDeviceId", "token-blah", null);

        when(userPushTokenRepository.findByUserIdAndDeviceId(userId, "testDeviceId"))
                .thenReturn(Optional.of(testToken));

        userService.deletePushToken(userId, "testDeviceId");

        verify(userPushTokenRepository).findByUserIdAndDeviceId(userId, "testDeviceId");
        verify(userPushTokenRepository).delete(testToken);
    }

    @Test
    @DisplayName("푸시토큰_삭제_실패_존재하지_않는_토큰")
    void deletePushToken_not_found() {
        User user = new User("test", "encoded");

        when(userPushTokenRepository.findByUserIdAndDeviceId(user.getId(), "testDeviceId"))
                .thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> userService.deletePushToken(user.getId(), "testDeviceId"));

        verify(userPushTokenRepository).findByUserIdAndDeviceId(user.getId(), "testDeviceId");
        verify(userPushTokenRepository, never()).delete(org.mockito.ArgumentMatchers.any());

    }
}
