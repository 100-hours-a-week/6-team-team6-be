package ktb.billage.security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ktb.billage.common.exception.ExceptionCode.AUTH_TOKEN_NOT_FOUND;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        log.warn("[AuthEntryPoint] uri={}, method={}", request.getRequestURI(), request.getMethod());

        if (authenticationException instanceof InsufficientAuthenticationException) {
            log.warn("[Base exception] code : {}", AUTH_TOKEN_NOT_FOUND.getCode());

            response.getWriter()
                    .write("{\"code\":\"%s\"}".formatted(AUTH_TOKEN_NOT_FOUND.getCode()));
            return;
        }

        log.warn("[Base exception] code : {}", authenticationException.getMessage());
        response.getWriter()
                .write("{\"code\":\"%s\"}".formatted(authenticationException.getMessage()));
    }
}
