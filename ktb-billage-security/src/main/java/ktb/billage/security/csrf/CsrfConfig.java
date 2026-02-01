package ktb.billage.security.csrf;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class CsrfConfig {

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(builder -> builder
                .sameSite("None")
                .secure(cookieSecure));
        return repository;
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
