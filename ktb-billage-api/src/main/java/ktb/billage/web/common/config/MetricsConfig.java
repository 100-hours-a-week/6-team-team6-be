package ktb.billage.web.common.config;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterFilter ignoreActuatorHttpServerRequests() {
        return MeterFilter.deny(id ->
                "http.server.requests".equals(id.getName())
                        && isIgnoredUri(id.getTag("uri"))
        );
    }

    private boolean isIgnoredUri(String uri) {
        return "/actuator/health".equals(uri) || "/actuator/prometheus".equals(uri);
    }
}
