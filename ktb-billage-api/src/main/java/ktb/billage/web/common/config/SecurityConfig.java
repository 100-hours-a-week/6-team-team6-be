package ktb.billage.web.common.config;

import ktb.billage.web.common.config.csrf.CsrfCookieIssuingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] ALLOWED_URL_LIST = new String[]{"/swagger**", "/auth/**"};

    @Qualifier("csrfProtectionMatcher")
    private final RequestMatcher csrfProtectionMatcher;
    private final CsrfTokenRepository csrfTokenRepository;
    private final CsrfCookieIssuingFilter csrfCookieIssuingFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ALLOWED_URL_LIST).permitAll()
                        .anyRequest().authenticated())

                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)

                .cors(withDefaults())

                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .requireCsrfProtectionMatcher(csrfProtectionMatcher))

                .addFilterAfter(csrfCookieIssuingFilter, CsrfFilter.class)

                .build();
    }
}
