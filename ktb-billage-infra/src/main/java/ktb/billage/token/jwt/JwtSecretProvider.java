package ktb.billage.token.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtSecretProvider {
    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtSecretProvider(
            @Value("${}") String key,
            @Value("${}") long accessTokenExpiration,
            @Value("${}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public SecretKey getKey() {
        return key;
    }

    public Date getAccessExpiration() {
        return new Date(new Date().getTime() + accessTokenExpiration);
    }

    public Date getRefreshExpiration() {
        return new Date(new Date().getTime() + refreshTokenExpiration);
    }
}
