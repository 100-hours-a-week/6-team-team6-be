package ktb.billage.domain.token;

public interface TokenGenerator {
    String generateAccessToken(Long userId);

    String generateRefreshToken(Long userId);

    String generateRefreshToken(String refreshToken);
}
