package ktb.billage.support;


import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.util.List;

@Testcontainers
public abstract class TestContainerSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    static MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0")
            .withDatabaseName("testDB")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @AfterEach
    void tearDownAcceptanceTestSupport() {
        truncateAllTables();
    }

    private void truncateAllTables() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'",
                String.class
        );

        for (String table : tables) {
            if (!"flyway_schema_history".equalsIgnoreCase(table)) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
