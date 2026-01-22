package ktb.billage.security.config;

import ktb.billage.security.filter.CsrfCookieIssuingFilter;
import ktb.billage.security.support.CsrfRequestMatchers;
import org.springframework.beans.factory.annotation.Qualifier;
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
            @Qualifier("csrfTokenIssueMatcher") RequestMatcher csrfTokenIssueMatcher
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
