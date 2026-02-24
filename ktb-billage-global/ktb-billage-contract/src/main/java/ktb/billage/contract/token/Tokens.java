package ktb.billage.contract.token;

import lombok.Getter;

@Getter
public final class Tokens {
    private final String accessToken;
    private final String refreshToken;
    private final Long userId;

    public Tokens(
            String accessToken,
            String refreshToken
    ) {
        this(accessToken, refreshToken, null);
    }

    public Tokens(
            String accessToken,
            String refreshToken,
            Long userId
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }
}
