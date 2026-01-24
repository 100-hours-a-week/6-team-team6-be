package ktb.billage.infra.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import ktb.billage.contract.auth.TokenParser;
import ktb.billage.common.exception.AuthException;
import org.springframework.stereotype.Component;

import static ktb.billage.common.exception.ExceptionCode.EXPIRED_RTOKEN;

@Component
public class JwtParser implements TokenParser {
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

    public void validateExpiration(String token) {
        try {
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new AuthException(EXPIRED_RTOKEN);
        }
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
