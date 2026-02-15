package ktb.billage.api.post;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import ktb.billage.web.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;

@AcceptanceTest
@Import(GlobalExceptionHandler.class)
class PostAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private Fixtures fixtures;

    private User user;
    private String accessToken;
    private Group group;
    private Membership myMembership;

    @BeforeEach
    void setUp() {
        user = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(user);
        group = fixtures.그룹_생성("test group");
        myMembership = fixtures.그룹_가입(group, user);
    }

    @Nested
    class 게시글_생성_테스트 {

        @Test
        @DisplayName("게시글 생성 성공 시나리오")
        void create_post_success() {
            String title = "물품 등록 제목";

            // when
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "title": "%s",
                              "content": "깨끗하게 사용했습니다",
                              "imageUrls": ["img-1"],
                              "rentalFee": 10000,
                              "feeUnit": "HOUR"
                            }
                            """.formatted(title))
                    .when()
                    .post("/groups/{groupId}/posts", group.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", notNullValue())
                    .extract()
                    .response();

            Long postId = response.jsonPath().getLong("postId");

            // then
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts/{postId}", group.getId(), postId)
                    .then()
                    .body("title", equalTo(title));
        }

        @Test
        @DisplayName("게시글 생성 실패 시나리오 - 그룹 멤버 아님")
        void create_post_fail_not_member() {
            fixtures.그룹_탈퇴(group, user);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "title": "드릴 빌려요",
                              "content": "깨끗하게 사용했습니다",
                              "imageUrls": ["img-1"],
                              "rentalFee": 10000,
                              "feeUnit": "HOUR"
                            }
                            """)
                    .when()
                    .post("/groups/{groupId}/posts", group.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }

        @Test
        @DisplayName("게시글 생성 실패 시나리오 - 요청 바디 검증 실패")
        void create_post_fail_invalid_body() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "title": "",
                              "content": "",
                              "imageUrls": [],
                              "rentalFee": -1,
                              "feeUnit": "HOUR"
                            }
                            """)
                    .when()
                    .post("/groups/{groupId}/posts", group.getId())
                    .then()
                    .statusCode(400)
                    .body("code", equalTo("PARAMETER01"));
        }
    }

    @Nested
    class 게시글_수정_테스트 {

        @Test
        @DisplayName("게시글 수정 성공 시나리오")
        void update_post_success() {
            String updatedTitle = "수정된 제목";

            Post post = fixtures.게시글_생성(myMembership);

            Response detail = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            Long postImageId = detail.jsonPath().getLong("imageUrls.imageInfos[0].postImageId");

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "title": "%s",
                              "content": "수정된 내용",
                              "imageUrls": [
                                { "postImageId": %d, "imageUrl": "img-1" },
                                { "postImageId": null, "imageUrl": "img-2" }
                              ],
                              "rentalFee": 20000,
                              "feeUnit": "DAY"
                            }
                            """.formatted(updatedTitle, postImageId))
                    .when()
                    .put("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(post.getId().intValue()));

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .extract()
                    .response();

            String newTitle = response.jsonPath().getString("title");
            assertThat(newTitle).isEqualTo(updatedTitle);

            assertThat(response.jsonPath().getList("imageUrls.imageInfos")).hasSize(2);
        }

        @Test
        @DisplayName("게시글 수정 실패 시나리오 - 작성자 아님")
        void update_post_fail_not_owner() {
            User another = fixtures.또_다른_유저_생성();
            Membership anotherMembership = fixtures.그룹_가입(group, another);

            Post anotherPost = fixtures.게시글_생성(anotherMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            {
                              "title": "수정된 제목",
                              "content": "수정된 내용",
                              "imageUrls": [
                                { "postImageId": null, "imageUrl": "img-2" }
                              ],
                              "rentalFee": 20000,
                              "feeUnit": "DAY"
                            }
                            """)
                    .when()
                    .put("/groups/{groupId}/posts/{postId}", group.getId(), anotherPost.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("POST02"));
        }
    }

    @Nested
    class 게시글_대여상태_변경_테스트 {

        @Test
        @DisplayName("게시글 대여 상태 변경 성공 시나리오")
        void change_rental_status_success() {
            // given
            Post post = fixtures.게시글_생성(myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .body("rentalStatus", equalTo("AVAILABLE"));

            // when, then
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .contentType("application/json")
                    .body("""
                            { "status": "RENTED_OUT" }
                            """)
                    .when()
                    .patch("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(post.getId().intValue()))
                    .body("rentalStatus", equalTo("RENTED_OUT"));
        }
    }

    @Nested
    class 게시글_삭제_테스트 {

        @Test
        @DisplayName("게시글 삭제 성공 시나리오")
        void delete_post_success() {
            Post post = fixtures.게시글_생성(myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts/{postId}", group.getId(), post.getId())
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    class 게시글_목록_조회_테스트 {

        @Test
        @DisplayName("그룹에서 게시글 목록 조회 성공 시나리오 - 삭제된 게시글")
        void get_posts_success_with_deleted_post() {
            User another = fixtures.또_다른_유저_생성();
            Membership anotherMembership = fixtures.그룹_가입(group, another);

            List<Post> myPosts = fixtures.여러_게시글_생성(myMembership, 10);
            fixtures.게시글_일부_삭제(myPosts, i -> i % 5 == 0);

            List<Post> anotherPosts = fixtures.여러_게시글_생성(anotherMembership, 10);
            fixtures.게시글_일부_삭제(anotherPosts, i -> i % 5 == 0);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts", group.getId())
                    .then()
                    .statusCode(200)
                    .body("summaries", notNullValue())
                    .body("nextCursor", nullValue())
                    .body("hasNextPage", equalTo(false))
                    .extract()
                    .response();

            int postCount = response.jsonPath().getList("summaries").size();
            assertThat(postCount).isEqualTo(16);

            Instant firstUpdatedPostAt = response.jsonPath().getList("summaries", PostResponse.Summary.class).getLast().updatedAt();
            Instant lastUpdatedPostAt = response.jsonPath().getList("summaries", PostResponse.Summary.class).getFirst().updatedAt();
            assertThat(firstUpdatedPostAt).isBefore(lastUpdatedPostAt);
        }

        @Test
        @DisplayName("그룹에서 게시글 목록 조회 성공 시나리오 - 커서를 이용한 조회")
        void get_posts_success_with_cursor() {
            User another = fixtures.또_다른_유저_생성();
            Membership anotherMembership = fixtures.그룹_가입(group, another);

            fixtures.여러_게시글_생성(myMembership, 30);
            fixtures.여러_게시글_생성(anotherMembership, 30);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts", group.getId())
                    .then()
                    .statusCode(200)
                    .body("summaries", notNullValue())
                    .body("nextCursor", notNullValue())
                    .extract()
                    .response();

            String nextCursor = response.jsonPath().getString("nextCursor");

            // when
            Response result = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts?cursor=%s".formatted(nextCursor), group.getId())
                    .then()
                    .statusCode(200)
                    .body("summaries", hasSize(20))
                    .body("nextCursor", notNullValue())
                    .body("hasNextPage", equalTo(true))
                    .extract()
                    .response();

            Instant firstUpdatedPostAt = result.jsonPath().getList("summaries", PostResponse.Summary.class).getLast().updatedAt();
            Instant lastUpdatedPostAt = result.jsonPath().getList("summaries", PostResponse.Summary.class).getFirst().updatedAt();
            assertThat(firstUpdatedPostAt).isBefore(lastUpdatedPostAt);
        }

        @Test
        @DisplayName("그룹에서 게시글 목록 조회 성공 시나리오 - 빈 게시글 리스트")
        void get_posts_success_with_empty_post_list() {

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts", group.getId())
                    .then()
                    .statusCode(200)
                    .body("summaries", emptyCollectionOf(PostResponse.Summary.class))
                    .body("nextCursor", nullValue())
                    .body("hasNextPage", equalTo(false));
        }

        @Test
        @DisplayName("게시글 검색 조회 성공 시나리오")
        void get_posts_by_keyword_success() {
            String keyword = "keyword";
            String titleWithKeyword = "title-" + keyword + "-suffix";

            User another = fixtures.또_다른_유저_생성();
            Membership anotherMembership = fixtures.그룹_가입(group, another);

            List<Post> myPosts = fixtures.여러_게시글_생성(myMembership, 10);
            List<Post> anotherPosts = fixtures.여러_게시글_생성(anotherMembership, 10);
            List<Post> evenPosts = List.of(myPosts, anotherPosts).stream()
                    .flatMap(List::stream)
                    .filter(post -> post.getId() % 2 == 0)
                    .toList();

            fixtures.여러_게시글_제목_수정(evenPosts, titleWithKeyword);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/groups/{groupId}/posts?query=%s".formatted(keyword), group.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            List<String> resultList = response.jsonPath().getList("summaries", PostResponse.Summary.class).stream()
                    .map(post -> post.postTitle())
                    .toList();

            assertThat(resultList).allMatch(item -> item.contains(keyword));
        }
    }

    @Nested
    class 게시글_상세_조회_테스트 {

        @Test
        @DisplayName("게시글 상세 조회 - 판매자 시나리오")
        void get_post_detail_seller() {

           // TODO. 채팅방 생성을 포함해서 작성 필요
        }

        @Test
        @DisplayName("게시글 상세 조회 - 구매자 시나리오")
        void get_post_detail_buyer() {
            // TODO. 채팅방 포함해서 작성 필요
        }
    }

    @Nested
    class 내_게시글_조회_테스트 {

        @Test
        void 게시글_20개_이하_조회() {
            // given
            for (int i = 1; i <= 2; i++) {
                Group group = fixtures.그룹_생성(String.valueOf(i));
                Membership membership = fixtures.그룹_가입(group, user);

                List<Post> posts = fixtures.여러_게시글_생성(membership, 10);
                fixtures.게시글_일부_삭제(posts, index -> (index + 1) % 5 == 0);
            }

            // when then
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("summaries")).hasSize(16);
            assertThat(response.jsonPath().getString("nextCursor")).isNull();
            assertThat(response.jsonPath().getBoolean("hasNextPage")).isFalse();

            String firstUpdatedAt = response.jsonPath().getString("summaries[0].updatedAt");
            String lastUpdatedAt = response.jsonPath().getString("summaries[-1].updatedAt");
            assertThat(Instant.parse(firstUpdatedAt)).isAfter(Instant.parse(lastUpdatedAt));
        }

        @Test
        void 게시글_20개_이상_조회_커서_사용() {
            // given
            for (int i = 1; i <= 3; i++) {
                Group group = fixtures.그룹_생성("cursor-" + i);
                Membership membership = fixtures.그룹_가입(group, user);
                fixtures.여러_게시글_생성(membership, 10);
            }

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts")
                    .then()
                    .statusCode(200)
                    .body("summaries", hasSize(20))
                    .body("nextCursor", notNullValue())
                    .body("hasNextPage", equalTo(true))
                    .extract()
                    .response();

            String nextCursor = response.jsonPath().getString("nextCursor");

            // when
            Response result = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts?cursor=%s".formatted(nextCursor))
                    .then()
                    .statusCode(200)
                    .body("summaries", hasSize(10))
                    .body("nextCursor", nullValue())
                    .body("hasNextPage", equalTo(false))
                    .extract()
                    .response();

            String firstUpdatedAt = result.jsonPath().getString("summaries[0].updatedAt");
            String lastUpdatedAt = result.jsonPath().getString("summaries[-1].updatedAt");
            assertThat(Instant.parse(firstUpdatedAt)).isAfter(Instant.parse(lastUpdatedAt));
        }
    }
}
