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
    protected int port;

    @BeforeEach
    void setUpAcceptanceTestSupport() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }


}
