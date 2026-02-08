package ktb.billage.api.group;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@AcceptanceTest
public class GroupAcceptanceTest extends AcceptanceTestSupport {
    private static final int USER_GROUP_LIMIT = 30;
    private static final int MAX_GROUP_MEMBER_COUNT = 3000;

    @Autowired
    private Fixtures fixtures;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(user);
    }

    @Nested
    class 그룹_생성_테스트 {

        @Test
        @DisplayName("그룹 생성 성공 시나리오")
        void create_success() {
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "groupName" : "test group",
                              "groupCoverImageUrl" : "dummy.png"
                            }
                            """)
                    .when()
                    .post("/groups")
                    .then()
                    .statusCode(201)
                    .body("groupId", notNullValue())
                    .extract()
                    .response();

            long groupId = response.jsonPath().getLong("groupId");

            Response myGroups = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/groups")
                    .then()
                    .extract()
                    .response();

            List<Long> groupIds = myGroups.jsonPath().getList("groupSummaries.groupId", Long.class);
            assertThat(groupIds).contains(groupId);
        }

        @Test
        @DisplayName("그룹 생성 실패 시나리오 - 사용자의 최대 소속 그룹 개수 초과")
        void create_fail_over_group_limit() {
            fixtures.소속_그룹_벌크_생성(user, USER_GROUP_LIMIT);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "groupName" : "test group",
                              "groupCoverImageUrl" : "dummy.png"
                            }
                            """)
                    .when()
                    .post("/groups")
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP05"));

            Response myGroups = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/groups")
                    .then()
                    .extract()
                    .response();

            int totalMyGroupsCount = myGroups.jsonPath().getInt("totalCount");
            assertThat(totalMyGroupsCount).isEqualTo(USER_GROUP_LIMIT);
        }
    }

    @Nested
    class 초대장_생성_시나리오_테스트 {

        @Test
        void 성공() {
            String groupName = "invite-group";
            Group group = fixtures.그룹_생성(groupName);
            fixtures.그룹_가입(group, user);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/groups/{groupId}/invitations", group.getId())
                    .then()
                    .statusCode(201)
                    .body("invitationToken", notNullValue())
                    .extract()
                    .response();

            String invitationToken = response.jsonPath().getString("invitationToken");

            User another = fixtures.또_다른_유저_생성();
            String anotherToken = fixtures.토큰_생성(another);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherToken)
                    .when()
                    .post("/invitations/{invitationToken}", invitationToken)
                    .then()
                    .body("groupName", equalTo(groupName));
        }

        @Test
        void 실패_유효하지_않은_그룹() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/groups/{groupId}/invitations", 999999L)
                    .then()
                    .statusCode(404)
                    .body("code", equalTo("GROUP01"));
        }

        @Test
        void 실패_본인이_속하지_않은_그룹() {
            Group group = fixtures.그룹_생성("not-member-group");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/groups/{groupId}/invitations", group.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }
    }

    @Nested
    class 초대장_검증_테스트 {

        @Test
        void 성공() {
            User owner = fixtures.또_다른_유저_생성();
            String ownerToken = fixtures.토큰_생성(owner);
            Group group = fixtures.그룹_생성("invite-check-group");
            fixtures.그룹_가입(group, owner);

            String invitationToken = createInvitationToken(group.getId(), ownerToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/invitations/{invitationToken}", invitationToken)
                    .then()
                    .statusCode(200)
                    .body("groupId", equalTo(group.getId().intValue()))
                    .body("groupName", equalTo(group.getName()));
        }

        @Test
        void 실패_유효하지_않은_초대장() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/invitations/{invitationToken}", "invalid-token")
                    .then()
                    .statusCode(404)
                    .body("code", equalTo("GROUP06"));
        }

        @Test
        void 실패_이미_속해있는_그룹() {
            Group group = fixtures.그룹_생성("already-member-group");
            fixtures.그룹_가입(group, user);
            String invitationToken = createInvitationToken(group.getId(), accessToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/invitations/{invitationToken}", invitationToken)
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP03"));
        }

        @Test
        void 실패_사용자_소속_그룹_최대_개수_초과() {
            fixtures.소속_그룹_벌크_생성(user, USER_GROUP_LIMIT);

            User owner = fixtures.또_다른_유저_생성();
            String ownerToken = fixtures.토큰_생성(owner);
            Group group = fixtures.그룹_생성("limit-check-group");
            fixtures.그룹_가입(group, owner);
            String invitationToken = createInvitationToken(group.getId(), ownerToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/invitations/{invitationToken}", invitationToken)
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP05"));
        }

        @Test
        void 실패_그룹_최대_수용_인원_초과() {
            User owner = fixtures.또_다른_유저_생성();
            String ownerToken = fixtures.토큰_생성(owner);
            Group group = fixtures.그룹_생성("capacity-check-group");
            fixtures.그룹_가입(group, owner);
            fixtures.그룹원_벌크_생성(group, MAX_GROUP_MEMBER_COUNT - 1);

            String invitationToken = createInvitationToken(group.getId(), ownerToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/invitations/{invitationToken}", invitationToken)
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP04"));
        }
    }

    @Nested
    class 그룹_가입_테스트 {

        @Test
        void 성공() {
            String groupName = "join-group";

            User owner = fixtures.또_다른_유저_생성();
            String ownerToken = fixtures.토큰_생성(owner);
            Group group = fixtures.그룹_생성(groupName);
            fixtures.그룹_가입(group, owner);
            String invitationToken = createInvitationToken(group.getId(), ownerToken);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "member-nick"
                            }
                            """)
                    .when()
                    .post("/invitations/{invitationToken}/memberships", invitationToken)
                    .then()
                    .statusCode(201)
                    .body("membershipId", notNullValue())
                    .extract()
                    .response();

            Long membershipId = response.jsonPath().getLong("membershipId");
            assertThat(membershipId).isNotNull();

            List<String> myGroups = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/groups")
                    .then()
                    .extract()
                    .response()
                    .jsonPath().getList("groupSummaries.groupName", String.class);

            assertThat(myGroups).contains(groupName);

        }

        @Test
        void 실패_유효하지_않은_초대장() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "member-nick"
                            }
                            """)
                    .when()
                    .post("/invitations/{invitationToken}/memberships", "invalid-token")
                    .then()
                    .statusCode(404)
                    .body("code", equalTo("GROUP06"));
        }

        @Test
        void 실패_이미_속해있는_그룹() {
            Group group = fixtures.그룹_생성("already-join-group");
            fixtures.그룹_가입(group, user);

            String invitationToken = createInvitationToken(group.getId(), accessToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "member-nick"
                            }
                            """)
                    .when()
                    .post("/invitations/{invitationToken}/memberships", invitationToken)
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP03"));
        }

        @Test
        void 실패_사용자_소속_그룹_최대_개수_초과() {
            fixtures.소속_그룹_벌크_생성(user, USER_GROUP_LIMIT);

            User owner = fixtures.또_다른_유저_생성();
            String ownerToken = fixtures.토큰_생성(owner);
            Group group = fixtures.그룹_생성("limit-join-group");
            fixtures.그룹_가입(group, owner);
            String invitationToken = createInvitationToken(group.getId(), ownerToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "member-nick"
                            }
                            """)
                    .when()
                    .post("/invitations/{invitationToken}/memberships", invitationToken)
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP05"));
        }

        @Test
        void 실패_그룹_최대_수용_인원_초과() {
            User owner = fixtures.또_다른_유저_생성();
            String ownerToken = fixtures.토큰_생성(owner);
            Group group = fixtures.그룹_생성("capacity-join-group");
            fixtures.그룹_가입(group, owner);
            fixtures.그룹원_벌크_생성(group, MAX_GROUP_MEMBER_COUNT - 1);

            String invitationToken = createInvitationToken(group.getId(), ownerToken);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "member-nick"
                            }
                            """)
                    .when()
                    .post("/invitations/{invitationToken}/memberships", invitationToken)
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("GROUP04"));
        }
    }

    @Nested
    class 그룹_탈퇴_테스트 {

        @Test
        void 성공_마지막_남은_멤버가_아닌_경우_그룹은_남아있음() {
            String leaveGroupName = "leave-group";
            Group group = fixtures.그룹_생성(leaveGroupName);
            fixtures.그룹_가입(group, user);

            User other = fixtures.또_다른_유저_생성();
            fixtures.그룹_가입(group, other);
            String otherToken = fixtures.토큰_생성(other);

            // when
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(204);

            // then
            List<String> myGroups = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/groups")
                    .then()
                    .extract()
                    .response().jsonPath().getList("groupSummaries.groupName", String.class);

            assertThat(myGroups).doesNotContain(leaveGroupName);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + otherToken)
                    .when()
                    .get("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(200);
        }

        @Test
        void 성공_마지막_멤버일때_탈퇴_시_그룹이_삭제됨() {
            String leaveGroupName = "leave-last-group";
            Group group = fixtures.그룹_생성(leaveGroupName);
            fixtures.그룹_가입(group, user);

            // when
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(204);

            // then
            List<String> myGroups = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/groups")
                    .then()
                    .extract()
                    .response().jsonPath().getList("groupSummaries.groupName", String.class);

            assertThat(myGroups).doesNotContain(leaveGroupName);
        }

        @Test
        void 실패_유효하지_않은_그룹() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}", 999999L)
                    .then()
                    .statusCode(404)
                    .body("code", equalTo("GROUP01"));
        }
    }

    private String createInvitationToken(Long groupId, String token) {
        Response response = RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .when()
                .post("/groups/{groupId}/invitations", groupId)
                .then()
                .statusCode(201)
                .extract()
                .response();

        return response.jsonPath().getString("invitationToken");
    }


    @Nested
    class 내_그룹_조회_테스트 {

        @Test
        void 성공_소속된_그룹_목록_조회() {
            // given
            Group group1 = fixtures.그룹_생성("my-group-1");
            fixtures.그룹_가입(group1, user);
            Group group2 = fixtures.그룹_생성("my-group-2");
            fixtures.그룹_가입(group2, user);

            Group group3 = fixtures.그룹_생성("my-group-3");
            fixtures.그룹_가입(group3, user);
            fixtures.그룹_탈퇴(group3, user);

            // when
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/groups")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            int totalCount = response.jsonPath().getInt("totalCount");
            List<Long> groupIds = response.jsonPath().getList("groupSummaries.groupId", Long.class);
            assertThat(totalCount).isEqualTo(2);
            assertThat(groupIds).contains(group1.getId(), group2.getId());
        }
    }

    @Nested
    class 단일_그룹_프로필_조회_테스트 {

        @Test
        void 성공() {
            Group group = fixtures.그룹_생성("profile-group");
            fixtures.그룹_가입(group, user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(200)
                    .body("groupId", equalTo(group.getId().intValue()))
                    .body("groupName", equalTo(group.getName()));
        }

        @Test
        void 실패_유효하지_않은_그룹() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}", 999999L)
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }

        @Test
        void 실패_본인이_속하지_않은_그룹() {
            Group group = fixtures.그룹_생성("not-member-group");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }

        @Test
        void 실패_본인이_속하지_않은_그룹_탈퇴한_경우() {
            Group group = fixtures.그룹_생성("not-member-group");
            fixtures.그룹_가입(group, user);
            fixtures.그룹_탈퇴(group, user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }
    }

    @Nested
    class 그룹_내_나의_프로필_조회_테스트 {

        @Test
        void 성공() {
            Group group = fixtures.그룹_생성("my-profile-group");
            Membership membership = fixtures.그룹_가입(group, user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/membership/me", group.getId())
                    .then()
                    .statusCode(200)
                    .body("membershipId", equalTo(membership.getId().intValue()));
        }

        @Test
        void 실패_유효하지_않은_그룹() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/membership/me", 999999L)
                    .then()
                    .statusCode(404)
                    .body("code", equalTo("GROUP01"));
        }

        @Test
        void 실패_본인이_속하지_않은_그룹() {
            Group group = fixtures.그룹_생성("no-membership-group");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/membership/me", group.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }
    }

    @Nested
    class 그룹_내_닉네임_변경_테스트 {

        @Test
        void 성공() {
            String newNickname = "new-nick";

            Group group = fixtures.그룹_생성("change-nickname-group");
            fixtures.그룹_가입(group, user);

            // when
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "%s"
                            }
                            """.formatted(newNickname))
                    .when()
                    .patch("/groups/{groupId}/membership/me", group.getId())
                    .then()
                    .statusCode(200)
                    .body("nickname", equalTo("new-nick"));

            // then
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/membership/me", group.getId())
                    .then()
                    .body("nickname", equalTo(newNickname));
        }

        @Test
        void 실패_유효하지_않은_그룹() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "new-nick"
                            }
                            """)
                    .when()
                    .patch("/groups/{groupId}/membership/me", 999999L)
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }

        @Test
        void 실패_본인이_속하지_않은_그룹() {
            Group group = fixtures.그룹_생성("change-nickname-fail-group");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "nickname" : "new-nick"
                            }
                            """)
                    .when()
                    .patch("/groups/{groupId}/membership/me", group.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }
    }
}
