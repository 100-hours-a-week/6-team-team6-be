package ktb.billage.security.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationInjector jwtAuthenticationInjector) {

        return new JwtAuthenticationFilter(
                jwtAuthenticationEntryPoint,
                jwtAuthenticationInjector
        );
    }
}
