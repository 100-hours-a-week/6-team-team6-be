package ktb.billage.security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ktb.billage.common.exception.ExceptionCode.AUTH_TOKEN_NOT_FOUND;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        if (authException instanceof InsufficientAuthenticationException) {
            response.getWriter()
                    .write("{\"code\":\"%s\"}".formatted(AUTH_TOKEN_NOT_FOUND.getCode()));
            return;
        }

        response.getWriter()
                .write("{\"code\":\"%s\"}".formatted(authException.getMessage()));
    }
}
