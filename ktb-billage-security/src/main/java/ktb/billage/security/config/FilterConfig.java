package ktb.billage.security.config;

import ktb.billage.security.filter.JwtAuthenticationFilter;
import ktb.billage.security.handler.JwtAuthenticationEntryPoint;
import ktb.billage.security.support.JwtAuthenticationInjector;
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
