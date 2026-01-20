package ktb.billage.web.common.config.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CsrfCookieIssuingFilter extends OncePerRequestFilter {
    private final CsrfTokenRepository csrfTokenRepository;
    private final RequestMatcher requestMatcher;

    public CsrfCookieIssuingFilter(CsrfTokenRepository csrfTokenRepository, RequestMatcher requestMatcher) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.requestMatcher = requestMatcher;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        if (requestMatcher.matches(request)) {
            CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
            csrfTokenRepository.saveToken(csrfToken, request, response);
        }
    }
}
