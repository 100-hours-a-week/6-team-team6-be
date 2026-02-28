package ktb.billage.infra.rabbitmq.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RabbitMqConfig {
    private final String host;
    private final int port;
    private final String virtualHost;
    private final String username;
    private final String password;

    public RabbitMqConfig(
            @Value("${rabbitmq.host}") String host,
            @Value("${rabbitmq.port}") String port,
            @Value("${rabbitmq.virtual-host}") String virtualHost,
            @Value("${rabbitmq.username}") String username,
            @Value("${rabbitmq.password}") String password
    ) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.virtualHost = virtualHost;
        this.username = username;
        this.password = password;
    }
}
