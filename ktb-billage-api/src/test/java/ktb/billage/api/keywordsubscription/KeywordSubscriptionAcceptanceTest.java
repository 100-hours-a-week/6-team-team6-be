package ktb.billage.api.keywordsubscription;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.notNullValue;

@AcceptanceTest
public class KeywordSubscriptionAcceptanceTest extends AcceptanceTestSupport {
    @Autowired
    private Fixtures fixtures;

    private User me;
    private String accessToken;
    private Group group;

    @BeforeEach
    void setUp() {
        me = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(me);
        group = fixtures.그룹_생성("test");
        fixtures.그룹_가입(group, me);
    }

    @Nested
    class 키워드_구독_등록_테스트 {

        @Test
        @DisplayName("등록 성공")
        void success_register() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                      {
                        "keyword" : "키워드"
                      }
                    """)
                    .when()
                    .post("/groups/{groupId}/memberships/me/keyword-subscriptions", group.getId())
                    .then()
                    .statusCode(201)
                    .body("keywordSubscriptionId", notNullValue());
        }

        @Test
        @DisplayName("등록 실패 - 그룹이 존재하지 않음")
        void fail_register_non_existing_group() {
            Long nonExistentGroupId = 999999L;

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                      {
                        "keyword" : "키워드"
                      }
                    """)
                    .when()
                    .post("/groups/{groupId}/memberships/me/keyword-subscriptions", nonExistentGroupId)
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("등록 실패 - 가입한 그룹이 아닌 경우")
        void fail_register_not_joined_group() {
            fixtures.그룹_탈퇴(group, me);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                      {
                        "keyword" : "키워드"
                      }
                    """)
                    .when()
                    .post("/groups/{groupId}/memberships/me/keyword-subscriptions", group.getId())
                    .then()
                    .statusCode(403);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "한", "서른글자가 넘는 단어는 키워드 구독에 등록할 수 없습니다."})
        @DisplayName("등록 실패 - 키워드 형식 오류")
        void fail_parameter_error(String value) {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                              {
                                "keyword" : "%s"
                              }
                            """.formatted(value))
                    .when()
                    .post("/groups/{groupId}/memberships/me/keyword-subscriptions", group.getId())
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("등록 실패 - 이미 등록된 키워드")
        void fail_already_registered() {
            String alreadyKeword = "키워드";
            fixtures.키워드_구독_등록(me, group, alreadyKeword);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                              {
                                "keyword" : "%s"
                              }
                            """.formatted(alreadyKeword))
                    .when()
                    .post("/groups/{groupId}/memberships/me/keyword-subscriptions", group.getId())
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("등록 실패 - 최대 개수인 30개 초과")
        void fail_over_max_count() {
            fixtures.키워드_구독_벌크_등록(me, group, 30);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                      {
                        "keyword" : "something"
                      }
                    """)
                    .when()
                    .post("/groups/{groupId}/memberships/me/keyword-subscriptions", group.getId())
                    .then()
                    .statusCode(409);
        }
    }

    @Nested
    class 키워드_구독_삭제_테스트 {

        @Test
        @DisplayName("삭제 성공")
        void success_delete_keyword_subscrption() {
            Long keywordSubscriptionId = fixtures.키워드_구독_등록(me, group, "키워드").getId();

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}",
                            group.getId(), keywordSubscriptionId)
                    .then()
                    .statusCode(204);
        }

        @Test
        @DisplayName("삭제 실패 - 존재하지 않는 그룹")
        void fail_delete_non_existing_group() {
            Long nonExistentGroupId = 999999L;
            Long keywordSubscriptionId = fixtures.키워드_구독_등록(me, group, "키워드").getId();

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}",
                            nonExistentGroupId, keywordSubscriptionId)
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("삭제 실패 - 그룹원이 아님")
        void fail_delete_not_group_member() {
            Long keywordSubscriptionId = fixtures.키워드_구독_등록(me, group, "키워드").getId();
            fixtures.그룹_탈퇴(group, me);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}",
                            group.getId(), keywordSubscriptionId)
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("삭제 실패 - 존재하지 않는 구독 정보")
        void fail_delete_non_existing_keyword_subscrption() {
            Long nonExistentKeywordSubscriptionId = 999999L;

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}",
                            group.getId(), nonExistentKeywordSubscriptionId)
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("삭제 실패 - 이미 삭제한 구독 정보")
        void fail_delete_already_deleted_keyword_subscrption() {
            Long keywordSubscriptionId = fixtures.키워드_구독_등록(me, group, "키워드").getId();

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}",
                            group.getId(), keywordSubscriptionId)
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}",
                            group.getId(), keywordSubscriptionId)
                    .then()
                    .statusCode(404);
        }
    }
}
