package ktb.billage.domain.auth.service;

import ktb.billage.domain.auth.RefreshToken;
import ktb.billage.domain.auth.RefreshTokenRepository;
import ktb.billage.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void save(User user, String payload) {
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(token -> token.reissue(payload),
                        () -> refreshTokenRepository.save(new RefreshToken(user, payload)));
    }
}
