package ktb.billage.api.notification;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import ktb.billage.common.exception.ExceptionCode;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import org.apache.http.auth.AUTH;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static ktb.billage.common.exception.ExceptionCode.NOTIFICATION_ALREAY_DELETED;
import static ktb.billage.common.exception.ExceptionCode.NOTIFICATION_NOT_FOUND;
import static ktb.billage.common.exception.ExceptionCode.NOTIFICATION_NOT_OWNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@AcceptanceTest
public class NotificationAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private Fixtures fixtures;
    
    private User me;
    private String accessToken;
    private Group group;
    private Membership myMembership;
    private Post myPost;

    private User another;
    private Post anotherPost;
    
    @BeforeEach
    void setUp() {
        me = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(me);
        group = fixtures.그룹_생성("test");
        myMembership = fixtures.그룹_가입(group, me);

        another = fixtures.또_다른_유저_생성();
        Membership anotherMembership = fixtures.그룹_가입(group, another);

        myPost = fixtures.게시글_생성(myMembership);
        anotherPost = fixtures.게시글_생성(anotherMembership);
    }

    @Nested
    class 알림_목록_조회_테스트 {

        @Test
        @DisplayName("조회 성공 - 알림 센터가 비어있을 경우")
        void success_empty_list() {
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("notifications")).isEmpty();
            assertThat(response.jsonPath().getString("nextCursor")).isNull();
            assertThat(response.jsonPath().getBoolean("hasNext")).isFalse();
        }
        
        @Test
        @DisplayName("조회 성공 - 알림은 최신 순부터 앞에 배치")
        void success_ordered_by_newest() {
            for (int i = 0; i < 20; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            Instant first = Instant.parse(response.path("notifications[0].createdAt"));
            Instant second = Instant.parse(response.path("notifications[1].createdAt"));

            assertThat(first).isAfterOrEqualTo(second);
        }

        @Test
        @DisplayName("조회 성공 - 알림이 20개 이하인 경우")
        void success_under_20_counts() {
            for (int i = 0; i < 10; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("notifications")).hasSizeLessThanOrEqualTo(20);
            assertThat(response.jsonPath().getBoolean("hasNext")).isFalse();
        }

        @Test
        @DisplayName("조회 성공 - 알림이 정확히 20개인 경우")
        void success_exactly_20_counts() {
            for (int i = 0; i < 20; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("notifications")).hasSize(20);
            assertThat(response.jsonPath().getBoolean("hasNext")).isFalse();
        }

        @Test
        @DisplayName("조회 성공 - 알림이 20개 이상인 경우의 첫 페이지")
        void success_over_20_counts_first_page() {
            for (int i = 0; i < 30; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("notifications")).hasSize(20);
            assertThat(response.jsonPath().getString("nextCursor")).isNotNull();
            assertThat(response.jsonPath().getBoolean("hasNext")).isTrue();
        }

        @Test
        @DisplayName("조회 성공 - 알림이 20개 이상인 경우의 첫 페이지가 아닌 경우(커서 이용)")
        void success_over_20_counts_not_first_page() {
            for (int i = 0; i < 30; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            String cursor = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("nextCursor");

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .queryParam("cursor", cursor)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("notifications")).hasSize(10);
        }

        @Test
        @DisplayName("조회 성공 - 삭제된 알림은 보이지 않음")
        void success_deleted_notifications_not_visible() {
            for (int i = 0; i < 10; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            Response beforeDeletionResponse = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(beforeDeletionResponse.jsonPath().getList("notifications")).hasSize(10);
            Long idBeforeDeletion = ((Number) beforeDeletionResponse.path("notifications[0].notificationId")).longValue();

            fixtures.알림_삭제(idBeforeDeletion);

            Response afterDeletionResponse = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(afterDeletionResponse.jsonPath().getList("notifications")).hasSize(9);
            Long idAfterDeletion = ((Number) afterDeletionResponse.path("notifications[0].notificationId")).longValue();

            assertThat(idAfterDeletion).isNotEqualTo(idBeforeDeletion);
        }
    }

    @Nested
    class 알림_삭제_테스트 {

        @Test
        @DisplayName("삭제 성공")
        void success_delete_notification() {
            for (int i = 0; i < 10; i++) {
                fixtures.알림_생성_게시글(me, group, anotherPost, i);
            }

            Response beforeResponse = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .extract()
                    .response();

            Long targetId = ((Number) beforeResponse.path("notifications[0].notificationId")).longValue();
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/notifications/" + targetId)
                    .then()
                    .statusCode(204);

            Response afterResponse = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/notifications")
                    .then()
                    .extract()
                    .response();

            Long nonTargetId = ((Number) afterResponse.path("notifications[0].notificationId")).longValue();
            int afterSize = afterResponse.jsonPath().getList("notifications").size();

            assertThat(nonTargetId).isNotEqualTo(targetId);
            assertThat(afterSize).isEqualTo(9);
        }

        @Test
        @DisplayName("삭제 실패 - 이미 삭제된 알림")
        void fail_delete_already_deleted_notification() {
            Notification notification = fixtures.알림_생성_게시글(me, group, anotherPost, 0);
            Long notificationId = notification.getId();

            fixtures.알림_삭제(notificationId);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/notifications/" + notificationId)
                    .then()
                    .statusCode(404)
                    .body("code", is(NOTIFICATION_ALREAY_DELETED.getCode()));
        }

        @Test
        @DisplayName("삭제 실패 - 본인의 알림이 아닌 경우")
        void fail_delete_notification_not_my_notification() {
            Notification notification = fixtures.알림_생성_게시글(another, group, myPost, 0);
            Long notificationId = notification.getId();

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/notifications/" + notificationId)
                    .then()
                    .statusCode(403)
                    .body("code", is(NOTIFICATION_NOT_OWNED.getCode()));
        }

        @Test
        @DisplayName("삭제 실패 - 존재하지 않는 알림")
        void fail_delete_notification_not_existing() {
            Long nonExistentNotificationId = 999999L;

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/users/me/notifications/" + nonExistentNotificationId)
                    .then()
                    .statusCode(404)
                    .body("code", is(NOTIFICATION_NOT_FOUND.getCode()));
        }
    }
}
