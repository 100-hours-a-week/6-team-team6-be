package ktb.billage.infra.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import ktb.billage.domain.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtGenerator implements TokenGenerator {
    private final JwtSecretProvider jwtSecretProvider;

    @Override
    public String generateAccessToken(Long userId) {
        Claims claims = Jwts.claims()
                .subject(String.valueOf(userId))
                .add(Map.of(
                        "tokenType", "ACCESS",
                        Claims.EXPIRATION, jwtSecretProvider.getAccessExpiration()
                ))
                .build();

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()

                .claims(claims)

                .signWith(jwtSecretProvider.getKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()

                .expiration(jwtSecretProvider.getRefreshExpiration())

                .signWith(jwtSecretProvider.getKey())
                .compact();

    }

    @Override
    public String generateRefreshToken(String refreshToken) {
        return "";
    }
}
