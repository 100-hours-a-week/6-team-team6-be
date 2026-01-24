package ktb.billage.security.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.billage.security.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static ktb.billage.common.exception.ExceptionCode.INVALID_TOKEN;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationInjector jwtAuthenticationInjector;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!token.startsWith(BEARER_PREFIX)) {
            handleAuthenticationException(request, response, new JwtAuthenticationException(INVALID_TOKEN.getCode()));
            return;
        }

        String subToken = token.substring(BEARER_PREFIX.length());

        try {
            jwtAuthenticationInjector.setAuthentication(subToken);
        } catch (AuthenticationException ex) {
            handleAuthenticationException(request, response, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleAuthenticationException(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException, ServletException {
        authenticationEntryPoint.commence(request, response, ex);
    }
}
