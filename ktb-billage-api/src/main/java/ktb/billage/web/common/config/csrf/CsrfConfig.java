package ktb.billage.web.common.config.csrf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class CsrfConfig {

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public CsrfCookieIssuingFilter csrfCookieIssuingFilter(
            CsrfTokenRepository csrfTokenRepository,
            RequestMatcher csrfTokenIssueMatcher
    ) {
        return new CsrfCookieIssuingFilter(csrfTokenRepository, csrfTokenIssueMatcher);
    }

    @Bean
    public RequestMatcher csrfProtectionMatcher(CsrfRequestMatchers csrfRequestMatchers) {
        return csrfRequestMatchers.csrfProtectionMatcher();
    }

    @Bean
    public RequestMatcher csrfTokenIssueMatcher(CsrfRequestMatchers csrfRequestMatchers) {
        return csrfRequestMatchers.csrfTokenIssueMatcher();
    }
}
