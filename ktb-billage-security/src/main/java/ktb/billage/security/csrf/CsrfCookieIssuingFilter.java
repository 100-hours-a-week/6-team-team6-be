package ktb.billage.security.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CsrfCookieIssuingFilter extends OncePerRequestFilter {
    private final CsrfTokenRepository csrfTokenRepository;
    private final RequestMatcher csrfTokenIssueMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (csrfTokenIssueMatcher.matches(request)) {
            CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
            csrfTokenRepository.saveToken(csrfToken, request, response);
        }

        filterChain.doFilter(request, response);
    }
}
