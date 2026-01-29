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
import static ktb.billage.common.exception.ExceptionCode.EXPIRED_TOKEN;
import static ktb.billage.common.exception.ExceptionCode.INVALID_TOKEN;

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

    public void validateRefreshTokenExpiration(String token) {
        try {
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new AuthException(EXPIRED_RTOKEN);
        }
    }

    private Jws<Claims> validate(String token) {
        try {
            return parser.parseSignedClaims(token);
        } catch (IllegalArgumentException | MalformedJwtException | SignatureException e) { // FIXME. 적절한 예외 응답 추가
            throw new AuthException(INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new AuthException(EXPIRED_TOKEN);
        }
    }
}
