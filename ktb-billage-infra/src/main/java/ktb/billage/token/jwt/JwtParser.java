package ktb.billage.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

@Component
public class JwtParser {
    private final io.jsonwebtoken.JwtParser parser;

    public JwtParser(JwtSecretProvider jwtSecretProvider) {
        this.parser = Jwts.parser()
                .verifyWith(jwtSecretProvider.getKey())
                .build();
    }

    public String parseId(String token) {
        return validate(token).getPayload()
                .getSubject();
    }

    private Jws<Claims> validate(String token) {
        try {
            return parser.parseSignedClaims(token);
        } catch (IllegalArgumentException e) { // FIXME. 적절한 예외 응답 추가
            return null;
        } catch (ExpiredJwtException e) {
            return null;
        } catch (MalformedJwtException | SignatureException e) {
            return null;
        }
    }
}
