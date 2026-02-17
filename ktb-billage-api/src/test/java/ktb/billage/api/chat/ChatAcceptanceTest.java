package ktb.billage.api.chat;

import io.restassured.RestAssured;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.hamcrest.Matchers.notNullValue;

@AcceptanceTest
@Import(GlobalExceptionHandler.class)
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

    @Test
    @DisplayName("채팅방 생성 성공 시나리오")
    void create_chatroom_success() {
        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .when()
                .post("/posts/{postId}/chatrooms", anotherPost.getId())
                .then()
                .statusCode(201)
                .body("chatroomId", notNullValue())
                .extract()
                .response();
    }

//    @Test
//    @DisplayName("채팅방 생성 실패 시나리오 - 자기 자신과 채팅")
//    void create_chatroom_fail_self_chat() {
//        Long sellerId = createUser("sellerB", "SellerB1!");
//        createMembership(1L, sellerId);
//        Long postId = createPost("sellerB", "SellerB1!", "전동드릴", "내용", "img-1");
//
//        String sellerToken = loginAndGetAccessToken("sellerB", "SellerB1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + sellerToken)
//                .when()
//                .post("/posts/{postId}/chatrooms", postId)
//                .then()
//                .statusCode(403)
//                .body("code", equalTo("CHAT01"));
//    }
//
//    @Test
//    @DisplayName("채팅방 생성 실패 시나리오 - 이미 존재하는 채팅방")
//    void create_chatroom_fail_already_existing() {
//        Long sellerId = createUser("sellerC", "SellerC1!");
//        createMembership(1L, sellerId);
//        Long buyerId = createUser("buyerC", "BuyerC1!");
//        createMembership(1L, buyerId);
//
//        Long postId = createPost("sellerC", "SellerC1!", "전동드릴", "내용", "img-1");
//        String buyerToken = loginAndGetAccessToken("buyerC", "BuyerC1!");
//
//        Response response = RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + buyerToken)
//                .when()
//                .post("/posts/{postId}/chatrooms", postId)
//                .then()
//                .statusCode(201)
//                .extract()
//                .response();
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + buyerToken)
//                .when()
//                .post("/posts/{postId}/chatrooms", postId)
//                .then()
//                .statusCode(409)
//                .body("code", equalTo("CHAT04"));
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 조회 성공 시나리오")
//    void get_messages_success() {
//        ChatFixture fixture = createChatFixture("sellerD", "SellerD1!", "buyerD", "BuyerD1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
//                .when()
//                .get("/posts/{postId}/chatrooms/{chatroomId}/messages", fixture.postId, fixture.chatroomId)
//                .then()
//                .statusCode(200)
//                .body("chatroomId", equalTo(fixture.chatroomId.intValue()))
//                .body("messageItems", notNullValue());
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 조회 실패 시나리오 - 참여자 아님")
//    void get_messages_fail_not_participating() {
//        ChatFixture fixture = createChatFixture("sellerE", "SellerE1!", "buyerE", "BuyerE1!");
//
//        Long otherUserId = createUser("otherE", "OtherE1!");
//        createMembership(1L, otherUserId);
//        String otherToken = loginAndGetAccessToken("otherE", "OtherE1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + otherToken)
//                .when()
//                .get("/posts/{postId}/chatrooms/{chatroomId}/messages", fixture.postId, fixture.chatroomId)
//                .then()
//                .statusCode(403)
//                .body("code", equalTo("CHAT03"));
//    }
//
//    @Test
//    @DisplayName("내 게시글 채팅방 목록 조회 성공 시나리오")
//    void get_chatrooms_by_my_post_success() {
//        ChatFixture fixture = createChatFixture("sellerF", "SellerF1!", "buyerF", "BuyerF1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.sellerToken)
//                .when()
//                .get("/users/me/posts/{postId}/chatrooms", fixture.postId)
//                .then()
//                .statusCode(200)
//                .body("chatroomSummaries", notNullValue());
//    }
//
//    @Test
//    @DisplayName("내 참여 채팅방 목록 조회 성공 시나리오")
//    void get_my_participating_chatrooms_success() {
//        ChatFixture fixture = createChatFixture("sellerG", "SellerG1!", "buyerG", "BuyerG1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
//                .when()
//                .get("/users/me/chatrooms")
//                .then()
//                .statusCode(200)
//                .body("chatroomSummaries", notNullValue());
//    }
//
//    @Test
//    @DisplayName("내 전체 채팅방 미읽음 수 조회 성공 시나리오")
//    void get_unread_count_success() {
//        ChatFixture fixture = createChatFixture("sellerH", "SellerH1!", "buyerH", "BuyerH1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.sellerToken)
//                .when()
//                .get("/users/me/chatrooms/unread-count")
//                .then()
//                .statusCode(200)
//                .body("unreadChatMesageCount", equalTo(1));
//    }
//
//    @Test
//    @DisplayName("채팅방에서 게시글 요약 조회 성공 시나리오")
//    void get_post_summary_in_chatroom_success() {
//        ChatFixture fixture = createChatFixture("sellerI", "SellerI1!", "buyerI", "BuyerI1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
//                .when()
//                .get("/posts/{postId}/chatrooms/{chatroomId}/post", fixture.postId, fixture.chatroomId)
//                .then()
//                .statusCode(200)
//                .body("postId", equalTo(fixture.postId.intValue()))
//                .body("partnerId", notNullValue());
//    }
//
//    @Test
//    @DisplayName("채팅방 ID로 게시글 ID 조회 성공 시나리오")
//    void get_post_id_by_chatroom_id_success() {
//        ChatFixture fixture = createChatFixture("sellerJ", "SellerJ1!", "buyerJ", "BuyerJ1!");
//
//        RestAssured.given()
//                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
//                .when()
//                .get("/chatrooms/{chatroomId}/post", fixture.chatroomId)
//                .then()
//                .statusCode(200)
//                .body("postId", equalTo(fixture.postId.intValue()));
//    }
}
