package ktb.billage.security;

import ktb.billage.security.auth.JwtAuthenticationFilter;
import ktb.billage.security.auth.JwtAuthenticationEntryPoint;
import ktb.billage.security.csrf.CsrfCookieIssuingFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] ALLOWED_URL_LIST = new String[]{"/swagger**", "/auth/**", "/actuator/**", "/user"};

    private final RequestMatcher csrfProtectionMatcher;
    private final CsrfTokenRepository csrfTokenRepository;
    private final CsrfCookieIssuingFilter csrfCookieIssuingFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(@Qualifier("csrfProtectionMatcher") RequestMatcher csrfProtectionMatcher,
                          CsrfTokenRepository csrfTokenRepository,
                          CsrfCookieIssuingFilter csrfCookieIssuingFilter,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.csrfProtectionMatcher = csrfProtectionMatcher;
        this.csrfTokenRepository = csrfTokenRepository;
        this.csrfCookieIssuingFilter = csrfCookieIssuingFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .build();
    }
}
