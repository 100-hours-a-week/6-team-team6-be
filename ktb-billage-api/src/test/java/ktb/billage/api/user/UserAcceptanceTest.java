package ktb.billage.api.user;

import io.restassured.RestAssured;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserPushToken;
import ktb.billage.domain.user.UserPushTokenRepository;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AcceptanceTest
class UserAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private UserPushTokenRepository userPushTokenRepository;

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
                    .get("/users/me/web-push")
                    .then()
                    .body("enabled", equalTo(true));

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
                    .get("/users/me/web-push")
                    .then()
                    .body("enabled", equalTo(false));
        }
    }

    @Nested
    class 푸시_토큰_등록_및_갱신_테스트 {

        @Test
        @DisplayName("푸시 토큰 등록 성공 시나리오")
        void update_push_token_success_create() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "platform": "WEB",
                              "deviceId": "device-1",
                              "newToken": "fcm-token-1"
                            }
                            """)
                    .when()
                    .post("/users/me/push-token")
                    .then()
                    .statusCode(204);

            List<UserPushToken> pushTokens = userPushTokenRepository.findAllByUserId(user.getId());
            assertEquals(1, pushTokens.size());
            assertEquals(UserPushToken.PushPlatform.WEB, pushTokens.getFirst().getPlatform());
            assertEquals("device-1", pushTokens.getFirst().getDeviceId());
            assertEquals("fcm-token-1", pushTokens.getFirst().getFcmToken());
        }

        @Test
        @DisplayName("푸시 토큰 갱신 성공 시나리오 - 같은 디바이스 토큰 변경")
        void update_push_token_success_update_same_device() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);

            userPushTokenRepository.save(new UserPushToken(
                    user,
                    UserPushToken.PushPlatform.WEB,
                    "device-1",
                    "old-token",
                    java.time.Instant.now()
            ));

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "platform": "WEB",
                              "deviceId": "device-1",
                              "newToken": "new-token"
                            }
                            """)
                    .when()
                    .post("/users/me/push-token")
                    .then()
                    .statusCode(204);

            List<UserPushToken> pushTokens = userPushTokenRepository.findAllByUserId(user.getId());
            assertEquals(1, pushTokens.size());
            assertEquals("new-token", pushTokens.getFirst().getFcmToken());
        }

        @Test
        @DisplayName("푸시 토큰 갱신 성공 시나리오 - 다른 유저의 동일 토큰을 현재 유저 디바이스로 재바인딩")
        void update_push_token_success_rebind_same_token_owner() {
            User oldOwner = fixtures.유저_생성();
            User newOwner = fixtures.또_다른_유저_생성();
            String accessToken = fixtures.토큰_생성(newOwner);

            String sharedToken = "shared-token";
            fixtures.유저_푸시_토큰_생성(oldOwner, UserPushToken.PushPlatform.IOS, "old-device", sharedToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "platform": "AOS",
                              "deviceId": "new-device",
                              "newToken": "%s"
                            }
                            """.formatted(sharedToken))
                    .when()
                    .post("/users/me/push-token")
                    .then()
                    .statusCode(204);

            List<UserPushToken> oldOwnerTokens = userPushTokenRepository.findAllByUserId(oldOwner.getId());
            List<UserPushToken> newOwnerTokens = userPushTokenRepository.findAllByUserId(newOwner.getId());

            assertTrue(oldOwnerTokens.isEmpty());
            assertEquals(1, newOwnerTokens.size());
            assertEquals(UserPushToken.PushPlatform.AOS, newOwnerTokens.getFirst().getPlatform());
            assertEquals("new-device", newOwnerTokens.getFirst().getDeviceId());
            assertEquals("shared-token", newOwnerTokens.getFirst().getFcmToken());
        }

        @Test
        @DisplayName("푸시 토큰 등록 실패 시나리오 - 요청 파라미터 검증 실패")
        void update_push_token_fail_invalid_parameter() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "platform": null,
                              "deviceId": "",
                              "newToken": ""
                            }
                            """)
                    .when()
                    .post("/users/me/push-token")
                    .then()
                    .statusCode(400)
                    .body("code", equalTo("PARAMETER01"));
        }
    }

    @Nested
    class 푸시_토큰_삭제_테스트 {

        @Test
        @DisplayName("푸시 토큰 삭제 성공 시나리오")
        void delete_push_token_success() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);

            String deviceToken = "deviceToken";
            fixtures.유저_푸시_토큰_생성(user, UserPushToken.PushPlatform.WEB, deviceToken, "delete-target-token");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/push-token/{deviceId}", deviceToken)
                    .then()
                    .statusCode(204);

            List<UserPushToken> pushTokens = userPushTokenRepository.findAllByUserId(user.getId());
            assertTrue(pushTokens.isEmpty());
        }

        @Test
        @DisplayName("푸시 토큰 삭제 실패 시나리오 - 존재하지 않는 deviceId")
        void delete_push_token_fail_not_found() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/push-token/{deviceId}", "missing-device")
                    .then()
                    .statusCode(404)
                    .body("code", equalTo("USER05"));
        }

        @Test
        @DisplayName("푸시 토큰 삭제 실패 시나리오 - 본인 토큰이 아닌 경우 조회에 실패해 400 오류")
        void delete_push_token_fail_not_owner() {
            User user = fixtures.유저_생성();
            String accessToken = fixtures.토큰_생성(user);
            User another = fixtures.또_다른_유저_생성();

            UserPushToken pushToken = fixtures.유저_푸시_토큰_생성(another, UserPushToken.PushPlatform.IOS, "deviceToken", "fcmToken");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/push-token/{deviceId}", pushToken.getDeviceId())
                    .then()
                    .statusCode(404);


        }
    }

}
