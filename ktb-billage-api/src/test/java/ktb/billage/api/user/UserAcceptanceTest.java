package ktb.billage.api.user;

import io.restassured.RestAssured;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@AcceptanceTest
class UserAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private Fixtures fixtures;

    @Nested
    class 회원가입_테스트 {

        @Test
        @DisplayName("회원가입 성공 시나리오 - 로그인 성공으로 검증")
        void join_success() {
            String loginId = "joinuser1";
            String rawPassword = "Joinuser1!";

            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                            {
                              "loginId": "%s",
                              "password": "%s"
                            }
                            """.formatted(loginId, rawPassword))
                    .when()
                    .post("/users")
                    .then()
                    .statusCode(201)
                    .body("userId", notNullValue());

            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                            {
                             "loginId": "%s",
                             "password" : "%s"
                            }
                            """.formatted(loginId, rawPassword))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("회원가입 실패 시나리오 - 중복 로그인 아이디")
        void join_fail_duplicate_login_id() {
            String duplicateLoginId = "dupuser1";
            fixtures.유저_생성(duplicateLoginId);

            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                            {
                              "loginId": "%s",
                              "password": "Dupass1!"
                            }
                            """.formatted(duplicateLoginId))
                    .when()
                    .post("/users")
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("USER04"));
        }

        @Test
        @DisplayName("회원가입 실패 시나리오 - 로그인 아이디 형식 오류")
        void join_fail_invalid_login_id() {
            String invalidLoginId = "ab";

            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                            {
                              "loginId": "%s",
                              "password": "Joinpass1!"
                            }
                            """.formatted(invalidLoginId))
                    .when()
                    .post("/users")
                    .then()
                    .statusCode(400)
                    .body("code", equalTo("PARAMETER01"));
        }

        @Test
        @DisplayName("회원가입 실패 시나리오 - 비밀번호 형식 오류")
        void join_fail_invalid_password() {
            String invalidPassword = "abcd123";

            RestAssured.given()
                    .contentType("application/json")
                    .body("""
                            {
                              "loginId": "testlogin",
                              "password": "%s"
                            }
                            """.formatted(invalidPassword))
                    .when()
                    .post("/users")
                    .then()
                    .statusCode(400)
                    .body("code", equalTo("PARAMETER01"));
        }
    }

    @Nested
    class 내_프로필_조회_테스트 {

        @Test
        @DisplayName("내 프로필 조회 성공 시나리오")
        void get_my_profile_success() {
            User user = fixtures.유저_생성("testlogin");
            String accessToken = fixtures.토큰_생성(user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me")
                    .then()
                    .statusCode(200)
                    .body("loginId", equalTo("testlogin"))
                    .body("avatarImageUrl", notNullValue());
        }

        @Test
        @DisplayName("내 프로필 조회 실패 시나리오 - 인증 토큰 없음")
        void get_my_profile_fail_unauthorized() {
            RestAssured.given()
                    .when()
                    .get("/users/me")
                    .then()
                    .statusCode(401)
                    .body("code", equalTo("AUTH02"));
        }
    }

    @Nested
    class 웹푸시_알림_변경_테스트 {

        @Test
        void 웹푸시_알림_변경() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);

            // true 로 변경 확인
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "enabled" : true
                            }
                            """)
                    .when()
                    .put("/users/me/web-push")
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me")
                    .then()
                    .body("webPushEnabled", equalTo(true));

            // false로 변경 후 한 번 더 확인
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                               "enabled" : false
                            }
                            """)
                    .when()
                    .put("/users/me/web-push")
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me")
                    .then()
                    .body("webPushEnabled", equalTo(false));
        }
    }
}
