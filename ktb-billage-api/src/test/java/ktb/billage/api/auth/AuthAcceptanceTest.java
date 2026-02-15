package ktb.billage.api.auth;

import io.restassured.RestAssured;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.domain.auth.RefreshTokenRepository;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserRepository;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import ktb.billage.web.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import io.restassured.response.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AcceptanceTest
class AuthAcceptanceTest extends AcceptanceTestSupport {

    private static final String LOGIN_ID = "testuser1";
    private static final String RAW_PASSWORD = "Testpass1234!";
    private static final String NICKNAME = "nick01";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Fixtures fixtures;

    private User user;

    @BeforeEach
    void setUp() {
        user = fixtures.유저_생성(LOGIN_ID, passwordEncoder.encode(RAW_PASSWORD));
    }

    @Nested
    class 로그인_테스트 {

        @Test
        @DisplayName("로그인 성공 시나리오")
        void login_success() {
            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .header("Set-Cookie", notNullValue())
                    .cookie("refreshToken", notNullValue())
                    .cookie("XSRF-TOKEN", notNullValue())
                    .body("accessToken", notNullValue())
                    .body("userId", equalTo(user.getId().intValue()));
        }

        @Test
        @DisplayName("로그인 실패 시나리오 - 비밀번호 불일치")
        void login_fail_wrong_password() {
            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "wrongpass"
                        }
                        """.formatted(LOGIN_ID))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(401)
                    .body("code", equalTo("AUTH01"));
        }

        @Test
        @DisplayName("로그인 실패 시나리오 - 존재하지 않는 아이디")
        void login_fail_unknown_login_id() {
            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "unknown01",
                          "password": "anypass123"
                        }
                        """)
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(401)
                    .body("code", equalTo("AUTH01"));
        }
    }

    @Nested
    class 토큰_재발급_테스트 {

        @Test
        @DisplayName("토큰 재발급 성공 시나리오")
        void reissue_success() {
            // given
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            // when
            String refreshToken = loginResponse.getCookie("refreshToken");
            String csrfToken = loginResponse.getCookie("XSRF-TOKEN");

            // then
            RestAssured.given()
                    .cookie("refreshToken", refreshToken)
                    .cookie("XSRF-TOKEN", csrfToken)
                    .header("X-XSRF-TOKEN", csrfToken)
                    .when()
                    .post("/auth/tokens")
                    .then()
                    .statusCode(200)
                    .header("Set-Cookie", notNullValue())
                    .cookie("refreshToken", notNullValue())
                    .cookie("XSRF-TOKEN", notNullValue())
                    .body("accessToken", notNullValue());
        }

        @Test
        @DisplayName("토큰 재발급 실패 시나리오 - refreshToken 누락")
        void reissue_fail_missing_refresh_token_cookie() {
            // given
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            // when
            String csrfToken = loginResponse.getCookie("XSRF-TOKEN");

            RestAssured.given()
                    .cookie("XSRF-TOKEN", csrfToken)
                    .when()
                    .post("/auth/tokens")
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("토큰 재발급 실패 시나리오 - csrf Token 누락")
        void reissue_fail_missing_csrf_token_cookie() {
            // given
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            // when
            String refreshToken = loginResponse.getCookie("refreshToken");

            RestAssured.given()
                    .cookie("refreshToken", refreshToken)
                    .when()
                    .post("/auth/tokens")
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    class 로그아웃_테스트 {
        @Test
        @DisplayName("로그아웃 성공 시나리오")
        void logout_success() {
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String accessToken = loginResponse.jsonPath().getString("accessToken");
            String refreshToken = loginResponse.getCookie("refreshToken");
            String csrfToken = loginResponse.getCookie("XSRF-TOKEN");

            assertTrue(refreshTokenRepository.findByUser(user).isPresent());

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .cookie("refreshToken", refreshToken)
                    .cookie("XSRF-TOKEN", csrfToken)
                    .header("X-XSRF-TOKEN", csrfToken)
                    .when()
                    .post("/auth/logout")
                    .then()
                    .statusCode(204);

            assertFalse(refreshTokenRepository.findByUser(user).isPresent());
        }

        @Test
        @DisplayName("로그아웃 성공 시나리오 - refreshToken 쿠키 누락")
        void logout_success_without_refresh_token() {
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String accessToken = loginResponse.jsonPath().getString("accessToken");
            String csrfToken = loginResponse.getCookie("XSRF-TOKEN");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .cookie("XSRF-TOKEN", csrfToken)
                    .header("X-XSRF-TOKEN", csrfToken)
                    .when()
                    .post("/auth/logout")
                    .then()
                    .statusCode(204);
        }

        @Test
        @DisplayName("로그아웃 실패 시나리오 - accessToken 누락")
        void logout_fail_without_access_token() {
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String refreshToken = loginResponse.getCookie("refreshToken");
            String csrfToken = loginResponse.getCookie("XSRF-TOKEN");

            RestAssured.given()
                    .cookie("XSRF-TOKEN", csrfToken)
                    .cookie("refreshToken", refreshToken)
                    .header("X-XSRF-TOKEN", csrfToken)
                    .when()
                    .post("/auth/logout")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("로그아웃 실패 시나리오 - csrf token 누락")
        void logout_fail_without_csrf_token() {
            Response loginResponse = RestAssured.given()
                    .contentType("application/json")
                    .body("""
                        {
                          "loginId": "%s",
                          "password": "%s"
                        }
                        """.formatted(LOGIN_ID, RAW_PASSWORD))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String accessToken = loginResponse.jsonPath().getString("accessToken");
            String refreshToken = loginResponse.getCookie("refreshToken");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .cookie("refreshToken", refreshToken)
                    .when()
                    .post("/auth/logout")
                    .then()
                    .statusCode(403);
        }
    }
}
