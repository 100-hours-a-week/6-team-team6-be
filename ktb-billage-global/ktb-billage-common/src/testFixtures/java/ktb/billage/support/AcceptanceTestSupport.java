package ktb.billage.support;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public abstract class AcceptanceTestSupport extends TestContainerSupport {
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String BEARER_PREFIX = "Bearer ";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpAcceptanceTestSupport() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
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
