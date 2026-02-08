package ktb.billage.api.chat;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserRepository;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import ktb.billage.web.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@AcceptanceTest
@Import(GlobalExceptionHandler.class)
class ChatAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> membershipIds = new ArrayList<>();
    private final List<Long> postIds = new ArrayList<>();
    private final List<Long> chatroomIds = new ArrayList<>();
    private final List<Long> chatMessageIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ensureGroupExists(1L, "group-1");
    }

    @AfterEach
    void tearDown() {
        for (Long messageId : chatMessageIds) {
            jdbcTemplate.update("DELETE FROM chat_message WHERE id = ?", messageId);
        }
        for (Long chatroomId : chatroomIds) {
            jdbcTemplate.update("DELETE FROM chatroom WHERE id = ?", chatroomId);
        }
        for (Long postId : postIds) {
            jdbcTemplate.update("DELETE FROM post_image WHERE post_id = ?", postId);
            jdbcTemplate.update("DELETE FROM post WHERE id = ?", postId);
        }
        for (Long membershipId : membershipIds) {
            jdbcTemplate.update("DELETE FROM membership WHERE id = ?", membershipId);
        }
        for (Long userId : userIds) {
            jdbcTemplate.update("DELETE FROM refresh_token WHERE user_id = ?", userId);
            userRepository.findById(userId).ifPresent(userRepository::delete);
        }
        chatMessageIds.clear();
        chatroomIds.clear();
        postIds.clear();
        membershipIds.clear();
        userIds.clear();
    }

    @Test
    @DisplayName("채팅방 생성 성공 시나리오")
    void create_chatroom_success() {
        Long sellerId = createUser("sellerA", "SellerA1!");
        createMembership(1L, sellerId);
        Long buyerId = createUser("buyerA", "BuyerA1!");
        createMembership(1L, buyerId);

        Long postId = createPost("sellerA", "SellerA1!", "전동드릴", "내용", "img-1");
        String buyerToken = loginAndGetAccessToken("buyerA", "BuyerA1!");

        Response response = RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + buyerToken)
                .when()
                .post("/posts/{postId}/chatrooms", postId)
                .then()
                .statusCode(201)
                .body("chatroomId", notNullValue())
                .extract()
                .response();

        Long chatroomId = response.jsonPath().getLong("chatroomId");
        chatroomIds.add(chatroomId);
    }

    @Test
    @DisplayName("채팅방 생성 실패 시나리오 - 자기 자신과 채팅")
    void create_chatroom_fail_self_chat() {
        Long sellerId = createUser("sellerB", "SellerB1!");
        createMembership(1L, sellerId);
        Long postId = createPost("sellerB", "SellerB1!", "전동드릴", "내용", "img-1");

        String sellerToken = loginAndGetAccessToken("sellerB", "SellerB1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + sellerToken)
                .when()
                .post("/posts/{postId}/chatrooms", postId)
                .then()
                .statusCode(403)
                .body("code", equalTo("CHAT01"));
    }

    @Test
    @DisplayName("채팅방 생성 실패 시나리오 - 이미 존재하는 채팅방")
    void create_chatroom_fail_already_existing() {
        Long sellerId = createUser("sellerC", "SellerC1!");
        createMembership(1L, sellerId);
        Long buyerId = createUser("buyerC", "BuyerC1!");
        createMembership(1L, buyerId);

        Long postId = createPost("sellerC", "SellerC1!", "전동드릴", "내용", "img-1");
        String buyerToken = loginAndGetAccessToken("buyerC", "BuyerC1!");

        Response response = RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + buyerToken)
                .when()
                .post("/posts/{postId}/chatrooms", postId)
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long chatroomId = response.jsonPath().getLong("chatroomId");
        chatroomIds.add(chatroomId);

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + buyerToken)
                .when()
                .post("/posts/{postId}/chatrooms", postId)
                .then()
                .statusCode(409)
                .body("code", equalTo("CHAT04"));
    }

    @Test
    @DisplayName("채팅 메시지 조회 성공 시나리오")
    void get_messages_success() {
        ChatFixture fixture = createChatFixture("sellerD", "SellerD1!", "buyerD", "BuyerD1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
                .when()
                .get("/posts/{postId}/chatrooms/{chatroomId}/messages", fixture.postId, fixture.chatroomId)
                .then()
                .statusCode(200)
                .body("chatroomId", equalTo(fixture.chatroomId.intValue()))
                .body("messageItems", notNullValue());
    }

    @Test
    @DisplayName("채팅 메시지 조회 실패 시나리오 - 참여자 아님")
    void get_messages_fail_not_participating() {
        ChatFixture fixture = createChatFixture("sellerE", "SellerE1!", "buyerE", "BuyerE1!");

        Long otherUserId = createUser("otherE", "OtherE1!");
        createMembership(1L, otherUserId);
        String otherToken = loginAndGetAccessToken("otherE", "OtherE1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + otherToken)
                .when()
                .get("/posts/{postId}/chatrooms/{chatroomId}/messages", fixture.postId, fixture.chatroomId)
                .then()
                .statusCode(403)
                .body("code", equalTo("CHAT03"));
    }

    @Test
    @DisplayName("내 게시글 채팅방 목록 조회 성공 시나리오")
    void get_chatrooms_by_my_post_success() {
        ChatFixture fixture = createChatFixture("sellerF", "SellerF1!", "buyerF", "BuyerF1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.sellerToken)
                .when()
                .get("/users/me/posts/{postId}/chatrooms", fixture.postId)
                .then()
                .statusCode(200)
                .body("chatroomSummaries", notNullValue());
    }

    @Test
    @DisplayName("내 참여 채팅방 목록 조회 성공 시나리오")
    void get_my_participating_chatrooms_success() {
        ChatFixture fixture = createChatFixture("sellerG", "SellerG1!", "buyerG", "BuyerG1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
                .when()
                .get("/users/me/chatrooms")
                .then()
                .statusCode(200)
                .body("chatroomSummaries", notNullValue());
    }

    @Test
    @DisplayName("내 전체 채팅방 미읽음 수 조회 성공 시나리오")
    void get_unread_count_success() {
        ChatFixture fixture = createChatFixture("sellerH", "SellerH1!", "buyerH", "BuyerH1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.sellerToken)
                .when()
                .get("/users/me/chatrooms/unread-count")
                .then()
                .statusCode(200)
                .body("unreadChatMesageCount", equalTo(1));
    }

    @Test
    @DisplayName("채팅방에서 게시글 요약 조회 성공 시나리오")
    void get_post_summary_in_chatroom_success() {
        ChatFixture fixture = createChatFixture("sellerI", "SellerI1!", "buyerI", "BuyerI1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
                .when()
                .get("/posts/{postId}/chatrooms/{chatroomId}/post", fixture.postId, fixture.chatroomId)
                .then()
                .statusCode(200)
                .body("postId", equalTo(fixture.postId.intValue()))
                .body("partnerId", notNullValue());
    }

    @Test
    @DisplayName("채팅방 ID로 게시글 ID 조회 성공 시나리오")
    void get_post_id_by_chatroom_id_success() {
        ChatFixture fixture = createChatFixture("sellerJ", "SellerJ1!", "buyerJ", "BuyerJ1!");

        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fixture.buyerToken)
                .when()
                .get("/chatrooms/{chatroomId}/post", fixture.chatroomId)
                .then()
                .statusCode(200)
                .body("postId", equalTo(fixture.postId.intValue()));
    }

    private ChatFixture createChatFixture(String sellerLoginId, String sellerPassword,
                                          String buyerLoginId, String buyerPassword) {
        Long sellerId = createUser(sellerLoginId, sellerPassword);
        Long sellerMembershipId = createMembership(1L, sellerId);
        Long buyerId = createUser(buyerLoginId, buyerPassword);
        Long buyerMembershipId = createMembership(1L, buyerId);

        Long postId = createPost(sellerLoginId, sellerPassword, "전동드릴", "내용", "img-1");
        String sellerToken = loginAndGetAccessToken(sellerLoginId, sellerPassword);
        String buyerToken = loginAndGetAccessToken(buyerLoginId, buyerPassword);

        Response response = RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + buyerToken)
                .when()
                .post("/posts/{postId}/chatrooms", postId)
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long chatroomId = response.jsonPath().getLong("chatroomId");
        chatroomIds.add(chatroomId);

        Long messageId = createChatMessage(chatroomId, buyerMembershipId, "안녕하세요");
        updateChatroomLastMessage(chatroomId, messageId);

        return new ChatFixture(postId, chatroomId, sellerToken, buyerToken);
    }

    private void ensureGroupExists(Long groupId, String groupName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM billage_group WHERE id = ?", Long.class, groupId);
        if (count != null && count == 0L) {
            jdbcTemplate.update("INSERT INTO billage_group (id, group_name) VALUES (?, ?)", groupId, groupName);
        }
    }

    private Long createUser(String loginId, String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        User user = userRepository.save(new User(loginId, encoded));
        userIds.add(user.getId());
        return user.getId();
    }

    private Long createMembership(Long groupId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO membership (group_id, user_id, created_at, updated_at, deleted_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)",
                groupId,
                userId
        );
        Long membershipId = jdbcTemplate.queryForObject(
                "SELECT id FROM membership WHERE group_id = ? AND user_id = ? AND deleted_at IS NULL",
                Long.class,
                groupId,
                userId
        );
        membershipIds.add(membershipId);
        return membershipId;
    }

    private String loginAndGetAccessToken(String loginId, String rawPassword) {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body("""
                    {
                      "loginId": "%s",
                      "password": "%s"
                    }
                    """.formatted(loginId, rawPassword))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getString("accessToken");
    }

    private Long createPost(String loginId, String rawPassword, String title, String content, String imageUrl) {
        String accessToken = loginAndGetAccessToken(loginId, rawPassword);
        Response response = RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .contentType("application/json")
                .body("""
                    {
                      "title": "%s",
                      "content": "%s",
                      "imageUrls": ["%s"],
                      "rentalFee": 10000,
                      "feeUnit": "HOUR"
                    }
                    """.formatted(title, content, imageUrl))
                .when()
                .post("/groups/1/posts")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long postId = response.jsonPath().getLong("postId");
        postIds.add(postId);
        return postId;
    }

    private Long createChatMessage(Long chatroomId, Long senderMembershipId, String content) {
        Long messageId = System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO chat_message (id, sender_id, chatroom_id, content, created_at, updated_at, deleted_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)",
                messageId,
                senderMembershipId,
                chatroomId,
                content
        );
        chatMessageIds.add(messageId);
        return messageId;
    }

    private void updateChatroomLastMessage(Long chatroomId, Long messageId) {
        jdbcTemplate.update(
                "UPDATE chatroom SET last_message_id = ? WHERE id = ?",
                messageId,
                chatroomId
        );
    }

    private record ChatFixture(Long postId, Long chatroomId, String sellerToken, String buyerToken) {
    }
}
