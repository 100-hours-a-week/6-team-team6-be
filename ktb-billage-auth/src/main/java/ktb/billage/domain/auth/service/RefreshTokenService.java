package ktb.billage.domain.auth.service;

import ktb.billage.contract.auth.TokenParser;
import ktb.billage.domain.auth.RefreshToken;
import ktb.billage.domain.auth.RefreshTokenRepository;
import ktb.billage.domain.user.User;
import ktb.billage.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.exception.ExceptionCode.INVALID_RTOKEN;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final TokenParser tokenParser;

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(User user, String payload) {
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(token -> token.reissue(payload),
                        () -> refreshTokenRepository.save(new RefreshToken(user, payload)));
    }

    public RefreshToken loadRefreshToken(String payload) {
        RefreshToken refreshToken = findByPayload(payload);
        tokenParser.validateExpiration(payload);

        return refreshToken;
    }

    private RefreshToken findByPayload(String payload) {
        return refreshTokenRepository.findByPayload(payload)
                .orElseThrow(() -> new AuthException(INVALID_RTOKEN));
    }
}
