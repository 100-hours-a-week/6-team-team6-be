package ktb.billage.api.chat;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import ktb.billage.application.post.event.PostUpsertPayload;
import ktb.billage.application.post.listener.AiSyncPort;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.post.Post;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@AcceptanceTest
@Import({GlobalExceptionHandler.class, ChatAcceptanceTest.MockAiSyncConfig.class})
class ChatAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private Fixtures fixtures;

    private User me;
    private String accessToken;
    private Membership myMembership;

    private Group group;

    private User another;
    private Membership anotherMembership;
    private Post anotherPost;

    @BeforeEach
    void setUp() {
        me = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(me);

        another = fixtures.또_다른_유저_생성();

        group = fixtures.그룹_생성("test group");
        myMembership = fixtures.그룹_가입(group, me);
        anotherMembership = fixtures.그룹_가입(group, another);

        anotherPost = fixtures.게시글_생성(anotherMembership);
    }

    @Nested
    class 채팅방_생성_테스트 {
        @Test
        @DisplayName("채팅하기 진행 - 아직 채팅 내역은 없을 때, 채팅방은 생성되었지만 채팅방 목록 조회 시 조회되지 않음")
        void create_chatroom_without_message() {
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/posts/{postId}/chatrooms", anotherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("chatroomId", notNullValue())
                    .extract()
                    .response();

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms")
                    .then()
                    .statusCode(200)
                    .body("chatroomSummaries", hasSize(0));
        }

        @Test
        @DisplayName("채팅하기 진행 - 메시지까지 보냈을 때, 채팅방 조회 시 정상적으로 조회됨")
        void create_chatroom_with_message() {
            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/posts/{postId}/chatrooms", anotherPost.getId())
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();

            long chatroomId = response.jsonPath().getLong("chatroomId");
            fixtures.채팅_전송(chatroomId, myMembership);

            Response messageResponse = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages",
                            anotherPost.getId(), chatroomId)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(messageResponse.jsonPath().getLong("chatroomId")).isEqualTo(chatroomId);
            assertThat(messageResponse.jsonPath().getList("messageItems")).hasSize(1);
        }

        @Test
        @DisplayName("채팅방 생성 실패 시나리오 - 자기 자신과 채팅")
        void create_chatroom_fail_self_chat() {
            Post myPost = fixtures.게시글_생성(myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/posts/{postId}/chatrooms", myPost.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("CHAT01"));
        }

        @Test
        @DisplayName("채팅방 생성 실패 시나리오 - 이미 존재하는 채팅방")
        void create_chatroom_fail_already_existing() {
            fixtures.채팅방_생성(anotherPost, myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .post("/posts/{postId}/chatrooms", anotherPost.getId())
                    .then()
                    .statusCode(409)
                    .body("code", equalTo("CHAT04"));
        }
    }

    @Nested
    class 채팅_메시지_조회_테스트 {
        Chatroom chatroom;

        @BeforeEach
        void 채팅_메시지_셋업() {
            chatroom = fixtures.채팅방_생성(anotherPost, myMembership);

            for (int i = 0; i < 10; i++) {
                fixtures.채팅_전송(chatroom, myMembership, i);
                fixtures.채팅_전송(chatroom, anotherMembership, i);
            }
        }

        @Test
        @DisplayName("채팅 메시지 조회 성공 - 정상 시나리오")
        void get_messages_success() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("chatroomId", equalTo(chatroom.getId().intValue()))
                    .body("messageItems", hasSize(20));
        }

        @Test
        @DisplayName("채팅 메시지 조회 성공 - 상대방의 게시글이 삭제된 경우")
        void get_messages_success_after_partner_post_deletion() {
            String anotherAccessToken = fixtures.토큰_생성(another);
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), anotherPost.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("chatroomId", equalTo(chatroom.getId().intValue()))
                    .body("messageItems", hasSize(20));
        }

        @Test
        @DisplayName("채팅 메시지 조회 성공 - 내 게시글이 삭제된 경우")
        void get_messages_success_after_my_post_deletion() {
            String anotherAccessToken = fixtures.토큰_생성(another);
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), anotherPost.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("chatroomId", equalTo(chatroom.getId().intValue()))
                    .body("messageItems", hasSize(20));
        }

        @Test
        @DisplayName("채팅 메시지 조회 성공 - 상대방이 그룹을 탈퇴한 경우")
        void get_messages_success_after_partner_left_group() {
            String anotherAccessToken = fixtures.토큰_생성(another);
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("chatroomId", equalTo(chatroom.getId().intValue()))
                    .body("messageItems", hasSize(20));
        }

        @Test
        @DisplayName("채팅 메시지 조회 실패 시나리오 - 참여자 아님")
        void get_messages_fail_not_participating() {
            User thirdUser = fixtures.유저_생성("thirdUser");
            String thirdToken = fixtures.토큰_생성(thirdUser);
            fixtures.그룹_가입(group, thirdUser);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + thirdToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("CHAT03"));
        }

        @Test
        @DisplayName("채팅 메시지 조회 실패 - 내가 그룹을 떠난 경우")
        void get_messages_fail_left_group() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/messages", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }
    }

    @Nested
    class 내_게시글의_채팅방_목록_조회_테스트 {
        Group otherGroup;
        Membership otherGroupMyMembership;
        Membership otherGroupAnotherMembership;

        Post myPost;
        Post otherGroupMyPost;
        Post otherGroupAnotherPost;

        Chatroom myChatroom;
        Chatroom anotherChatroom;
        Chatroom otherGroupMyChatroom;
        Chatroom otherGroupAnotherChatroom;

        @BeforeEach
        void 다른_그룹_및_게시글들_채팅_셋업() {
            otherGroup = fixtures.그룹_생성("group2");
            otherGroupMyMembership = fixtures.그룹_가입(otherGroup, me);
            otherGroupAnotherMembership = fixtures.그룹_가입(otherGroup, another);

            myPost = fixtures.게시글_생성(myMembership);
            otherGroupMyPost = fixtures.게시글_생성(otherGroupMyMembership);
            otherGroupAnotherPost = fixtures.게시글_생성(otherGroupAnotherMembership);

            myChatroom = 채팅_생성(myPost, anotherMembership);
            anotherChatroom = 채팅_생성(anotherPost, myMembership);
            otherGroupMyChatroom = 채팅_생성(otherGroupMyPost, otherGroupAnotherMembership);
            otherGroupAnotherChatroom = 채팅_생성(otherGroupAnotherPost, otherGroupMyMembership);
        }

        private Chatroom 채팅_생성(Post post, Membership membership) {
            Chatroom chatroom = fixtures.채팅방_생성(post, membership);
            fixtures.채팅_전송(chatroom.getId(), myMembership);
            return chatroom;
        }

        @Test
        @DisplayName("조회 성공 - 모두 정상 채팅방 시나리오")
        void get_chatrooms_by_my_post_success_general() {
            // test 1
            Response response1 = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts/{postId}/chatrooms", myPost.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response1.jsonPath().getList("chatroomSummaries")).hasSize(1);
            assertThat(response1.jsonPath().getLong("chatroomSummaries[0].chatroomId"))
                    .isEqualTo(myChatroom.getId());

            // test 2
            Response response2 = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts/{postId}/chatrooms", otherGroupMyPost.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response2.jsonPath().getList("chatroomSummaries")).hasSize(1);
            assertThat(response2.jsonPath().getLong("chatroomSummaries[0].chatroomId"))
                    .isEqualTo(otherGroupMyChatroom.getId());
        }

        @Test
        @DisplayName("조회 성공 - 나의 삭제된 게시글 포함 채팅방 시나리오: 동일하게 조회됨")
        void get_chatrooms_with_my_deletion_post_success() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), myPost.getId())
                    .then()
                    .statusCode(204);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts/{postId}/chatrooms", myPost.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("chatroomSummaries")).hasSize(1);
            assertThat(response.jsonPath().getLong("chatroomSummaries[0].chatroomId"))
                    .isEqualTo(myChatroom.getId());
        }

        @Test
        @DisplayName("조회 실패 - 내가 탈퇴한 그룹의 게시글의 채팅방은 조회되지 않음")
        void get_chatrooms_without_left_group_post_success() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}", otherGroup.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/posts/{postId}/chatrooms", otherGroupMyPost.getId())
                    .then()
                    .statusCode(403)
                    .body("code", equalTo("GROUP02"));
        }

        @Test
        @DisplayName("조회 성공 - 상대방이 탈퇴한 경우에도 정상적으로 조회되는 시나리오")
        void get_chatrooms_with_partner_left_group_post_success() {
            String anotherAccessToken = fixtures.토큰_생성(another);
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}", otherGroup.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms")
                    .then()
                    .statusCode(200)
                    .body("chatroomSummaries", hasSize(4));
        }
    }

    @Nested
    class 내가_참여중인_채팅방_목록_조회_테스트 {

        @Test
        @DisplayName("내 참여 채팅방 목록 조회 시나리오")
        void get_my_participating_chatrooms_success() {
            User thirdUser = fixtures.유저_생성("third");
            Membership thirdMembership = fixtures.그룹_가입(group, thirdUser);

            Group otherGroup = fixtures.그룹_생성("other group");
            Membership otherGroupMyMembership = fixtures.그룹_가입(otherGroup, me);
            Membership otherGroupAnotherMembership = fixtures.그룹_가입(otherGroup, another);
            Membership otherGroupThirdMembership = fixtures.그룹_가입(otherGroup, thirdUser);

            Chatroom myChatroom1 = 채팅_생성(anotherPost, myMembership);

            Post thirdPost = fixtures.게시글_생성(thirdMembership);
            채팅_생성(thirdPost, anotherMembership);

            Post otherGroupMyPost = fixtures.게시글_생성(otherGroupMyMembership);
            Chatroom myChatroom2 = 채팅_생성(otherGroupMyPost, otherGroupAnotherMembership);
            Chatroom myChatroom3 = 채팅_생성(otherGroupMyPost, otherGroupThirdMembership);

            Post otherGroupThirdPost = fixtures.게시글_생성(otherGroupThirdMembership);
            채팅_생성(otherGroupThirdPost, otherGroupAnotherMembership);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms")
                    .then()
                    .statusCode(200)
                    .body("chatroomSummaries", hasSize(3))
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("chatroomSummaries.chatroomId", Integer.class))
                    .containsExactlyInAnyOrder(
                            myChatroom1.getId().intValue(),
                            myChatroom2.getId().intValue(),
                            myChatroom3.getId().intValue()
                    );

        }

        private Chatroom 채팅_생성(Post post, Membership membership) {
            Chatroom chatroom = fixtures.채팅방_생성(post, membership);
            fixtures.채팅_전송(chatroom.getId(), myMembership);
            return chatroom;
        }

        @Test
        @DisplayName("삭제된 게시글을 포함한 내가 참여 중인 채팅방 목록 조회 시나리오")
        void get_my_participating_chatrooms_with_my_deletion_post_success() {
            Post myPost = fixtures.게시글_생성(myMembership);
            Chatroom myChatroom = 채팅_생성(myPost, anotherMembership);
            Chatroom anotherChatroom = 채팅_생성(anotherPost, myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), myPost.getId())
                    .then()
                    .statusCode(204);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms")
                    .then()
                    .statusCode(200)
                    .body("chatroomSummaries", hasSize(2))
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("chatroomSummaries.chatroomId", Integer.class))
                    .containsExactlyInAnyOrder(
                            myChatroom.getId().intValue(),
                            anotherChatroom.getId().intValue()
                    );
        }

        @Test
        @DisplayName("그룹을 탈퇴한 상대방과의 채팅방을 포함한 내가 참여 중인 채팅방 목록 조회 시나리오")
        void get_my_participating_chatrooms_with_partner_left_group_post_success() {
            Post myPost = fixtures.게시글_생성(myMembership);
            Chatroom myChatroom = 채팅_생성(myPost, anotherMembership);
            Chatroom anotherChatroom = 채팅_생성(anotherPost, myMembership);

            String anotherAccessToken = fixtures.토큰_생성(another);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(204);

            Response response = RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms")
                    .then()
                    .statusCode(200)
                    .body("chatroomSummaries", hasSize(2))
                    .extract()
                    .response();

            assertThat(response.jsonPath().getList("chatroomSummaries.chatroomId", Integer.class))
                    .containsExactlyInAnyOrder(
                            myChatroom.getId().intValue(),
                            anotherChatroom.getId().intValue()
                    );
        }

        @Test
        @DisplayName("참여 중인 채팅방이 없는 경우 빈 목록 리턴 시나리오")
        void get_when_no_participating_chatrooms_success() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms")
                    .then()
                    .statusCode(200)
                    .body("chatroomSummaries", hasSize(0));
        }
    }

    @Nested
    class 내가_참여중인_채팅방의_읽지_않은_전체_메시지_수_조회_테스트 {
        @Test
        @DisplayName("조회 성공 - 여러 채팅방의 미읽음 메시지 합계를 정확히 반환하는 시나리오")
        void get_unread_count_success() {
            Post myPost = fixtures.게시글_생성(myMembership);
            Chatroom myChatroom1 = fixtures.채팅방_생성(myPost, anotherMembership);
            fixtures.채팅_전송(myChatroom1.getId(), anotherMembership);
            fixtures.채팅_전송(myChatroom1.getId(), anotherMembership);

            Chatroom myChatroom2 = fixtures.채팅방_생성(anotherPost, myMembership);
            fixtures.채팅_전송(myChatroom2.getId(), anotherMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms/unread-count")
                    .then()
                    .statusCode(200)
                    .body("unreadChatMesageCount", equalTo(3));
        }

        @Test
        @DisplayName("조회 성공 - 읽음 처리 후 미읽음 메시지 합계가 감소하는 시나리오")
        void get_unread_count_after_read_success() {
            Chatroom myChatroom = fixtures.채팅방_생성(anotherPost, myMembership);
            fixtures.채팅_전송(myChatroom.getId(), anotherMembership);
            fixtures.채팅_전송(myChatroom.getId(), anotherMembership);

            Chatroom myChatroom2 = fixtures.채팅방_생성(anotherPost, myMembership);
            fixtures.채팅_전송(myChatroom2.getId(), anotherMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms/unread-count")
                    .then()
                    .statusCode(200)
                    .body("unreadChatMesageCount", equalTo(3));

            fixtures.채팅_전송(myChatroom.getId(), myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms/unread-count")
                    .then()
                    .statusCode(200)
                    .body("unreadChatMesageCount", equalTo(1));
        }

        @Test
        @DisplayName("조회 성공 - 참여 중인 채팅방이 없는 경우 0을 반환하는 시나리오")
        void get_unread_count_when_no_participating_chatrooms_success() {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms/unread-count")
                    .then()
                    .statusCode(200)
                    .body("unreadChatMesageCount", equalTo(0));
        }

        @Test
        @DisplayName("조회 성공 - 내가 보낸 메시지는 미읽음 합계에서 제외되는 시나리오")
        void get_unread_count_excludes_my_sent_messages_success() {
            Chatroom myChatroom = fixtures.채팅방_생성(anotherPost, myMembership);
            fixtures.채팅_전송(myChatroom.getId(), myMembership);
            fixtures.채팅_전송(myChatroom.getId(), myMembership);
            fixtures.채팅_전송(myChatroom.getId(), myMembership);

            Chatroom myChatroom2 = fixtures.채팅방_생성(anotherPost, myMembership);
            fixtures.채팅_전송(myChatroom2.getId(), anotherMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/users/me/chatrooms/unread-count")
                    .then()
                    .statusCode(200)
                    .body("unreadChatMesageCount", equalTo(1));
        }
    }

    @Nested
    class 채팅방의_게시글_요약_정보_조회_테스트 {

        private Chatroom 채팅_생성(Post post, Membership membership) {
            Chatroom chatroom = fixtures.채팅방_생성(post, membership);
            fixtures.채팅_전송(chatroom.getId(), myMembership);
            return chatroom;
        }

        @Test
        @DisplayName("조회 성공 - 정상 시나리오")
        void get_post_summary_in_chatroom_success() {
            Chatroom chatroom = 채팅_생성(anotherPost, myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/post", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(anotherPost.getId().intValue()))
                    .body("postTitle", equalTo(anotherPost.getTitle()))
                    .body("isPartnerLeftGroup", equalTo(false))
                    .body("isPostDeleted", equalTo(false));
        }

        @Test
        @DisplayName("조회 성공 - 나의 삭제된 게시글 채팅방 조회 시나리오")
        void get_my_deleted_post_summary_in_chatroom_success() {
            Post myPost = fixtures.게시글_생성(myMembership);
            Chatroom chatroom = 채팅_생성(myPost, anotherMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), myPost.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/post", myPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(myPost.getId().intValue()))
                    .body("isPartnerLeftGroup", equalTo(false))
                    .body("isPostDeleted", equalTo(true));
        }

        @Test
        @DisplayName("조회 성공 - 상대방의 삭제된 게시글 채팅방 조회 시나리오")
        void get_partner_deleted_post_summary_in_chatroom_success() {
            String anotherAccessToken = fixtures.토큰_생성(another);
            Chatroom chatroom = 채팅_생성(anotherPost, myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}/posts/{postId}", group.getId(), anotherPost.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{postId}/chatrooms/{chatroomId}/post", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(anotherPost.getId().intValue()))
                    .body("isPartnerLeftGroup", equalTo(false))
                    .body("isPostDeleted", equalTo(true));
        }

        @Test
        @DisplayName("조회 성공 - 그룹을 탈퇴한 상대방의 게시글 채팅방 조회 시나리오")
        void get_partner_left_group_post_summary_in_chatroom_success() {
            String anotherAccessToken = fixtures.토큰_생성(another);
            Chatroom chatroom = 채팅_생성(anotherPost, myMembership);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + anotherAccessToken)
                    .when()
                    .delete("/groups/{groupId}", group.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .when()
                    .get("/posts/{post_id}/chatrooms/{chatroomId}/post", anotherPost.getId(), chatroom.getId())
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(anotherPost.getId().intValue()))
                    .body("isPartnerLeftGroup", equalTo(true))
                    .body("isPostDeleted", equalTo(true));
        }
    }

    @TestConfiguration
    static class MockAiSyncConfig {
        @Bean
        @Primary
        AiSyncPort mockAiSyncAdapter() {
            return new MockAiSyncAdapter();
        }
    }

    static class MockAiSyncAdapter implements AiSyncPort {
        @Override
        public void syncCreated(PostUpsertPayload payload) {
            return;
        }

        @Override
        public void syncUpdated(PostUpsertPayload payload) {
            return;
        }

        @Override
        public void syncDeleted(Long postId) {
            return;
        }
    }
}
