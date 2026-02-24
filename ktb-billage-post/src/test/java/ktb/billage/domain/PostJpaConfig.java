package ktb.billage.domain;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "ktb.billage.domain.post",
        "ktb.billage.domain.user",
        "ktb.billage.domain.group",
        "ktb.billage.domain.membership"
})
@EntityScan(basePackages = {
        "ktb.billage.domain.post",
        "ktb.billage.domain.user",
        "ktb.billage.domain.group",
        "ktb.billage.domain.membership"
})
public class PostJpaConfig {
}
