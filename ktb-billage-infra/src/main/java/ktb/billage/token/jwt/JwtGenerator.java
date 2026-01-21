package ktb.billage.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtGenerator {
    private final JwtSecretProvider jwtSecretProvider;

    private String generateAccessToken(Long userId) {
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
}
