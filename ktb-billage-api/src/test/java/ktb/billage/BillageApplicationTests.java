package ktb.billage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ktb.billage.support.TestContainerSupport;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class BillageApplicationTests extends TestContainerSupport {

    @Test
    void contextLoads() {
    }

}
