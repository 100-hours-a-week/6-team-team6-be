package ktb.billage.domain;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "ktb.billage.domain.user"
})
@EntityScan(basePackages = {
        "ktb.billage.domain.user"
})
public class UserJpaConfig {
}
