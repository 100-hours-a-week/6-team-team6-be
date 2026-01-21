package ktb.billage.domain.auth.service;

import ktb.billage.domain.token.TokenGenerator;
import ktb.billage.domain.token.Tokens;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.port.PasswordEncoder;
import ktb.billage.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private PasswordEncoder passwordEncoder;
    private TokenGenerator tokenGenerator;

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public Tokens login(String loginId, String password) {
        User user = userService.findByLoginId(loginId);
        user.verifyPassword(passwordEncoder, password);

        Long userId = user.getId();
        String accessToken = tokenGenerator.generateAccessToken(userId);
        String refreshToken = tokenGenerator.generateRefreshToken(userId);

        refreshTokenService.save(user, refreshToken);

        return new Tokens(accessToken, refreshToken, user.getId());
    }
}
