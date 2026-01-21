package ktb.billage.domain.token;

public interface TokenGenerator {
    Tokens login();

    Tokens reissue(String refreshToken);
}
