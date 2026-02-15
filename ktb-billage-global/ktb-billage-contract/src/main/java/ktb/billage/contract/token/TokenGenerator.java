package ktb.billage.contract.token;

public interface TokenGenerator {
    String generateAccessToken(Long userId);

    String generateRefreshToken(Long userId);
}
