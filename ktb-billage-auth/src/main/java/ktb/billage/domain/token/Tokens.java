package ktb.billage.domain.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Tokens {
    private final String accessToken;
    private final String refreshToken;
    private final Long userId;
}
