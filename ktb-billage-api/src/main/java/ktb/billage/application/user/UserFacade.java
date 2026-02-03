package ktb.billage.application.user;

import ktb.billage.domain.user.User;
import ktb.billage.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public UserSummary getUserSummary(Long userId) {
        User user = userService.findById(userId);
        return new UserSummary(user.getNickname(), user.getAvatarUrl());
    }

    public record UserSummary(String nickname, String avatarUrl) {
    }
}
