package ktb.billage.api.ai;

import ktb.billage.common.exception.AiTimeoutException;
import ktb.billage.common.exception.ExceptionCode;
import ktb.billage.common.exception.InternalException;
import ktb.billage.fixture.Fixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.domain.post.ai.AiPostDraftClient;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;
import io.restassured.RestAssured;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static ktb.billage.common.exception.ExceptionCode.SERVER_ERROR;
import static ktb.billage.common.exception.ExceptionCode.TIME_OUT;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AiAcceptanceTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private AiPostDraftClient aiPostDraftClient;

    @Autowired
    private Fixtures fixtures;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        user = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(user);
    }

    @AfterEach
    void tearDown() {
        fixtures.유저_삭제(user);
    }

    @Test
    @DisplayName("AI 게시글 초안 생성 요청 시나리오 테스트 성공")
    void makePostDraftByAi_success() {
        given(aiPostDraftClient.requestPostDraft(anyList()))
                .willReturn(new PostResponse.PostDraft(
                        "AI 추천 제목",
                        "AI 추천 내용",
                        BigDecimal.ZERO,
                        "HOUR"
                ));

        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("image", "image-1.jpg", "dummy".getBytes(StandardCharsets.UTF_8), "image/jpeg")
                .when()
                .post("/ai/post-drafts")
                .then()
                .statusCode(200)
                .body("title", notNullValue())
                .body("content", notNullValue())
                .body("rentalFee", equalTo(0))
                .body("feeUnit", equalTo("HOUR"));
    }

    @Test
    @DisplayName("AI 게시글 초안 생성 요청 시나리오 테스트 실패 - 시간 초과")
    void makePostDraftByAi_timeout() {
        given(aiPostDraftClient.requestPostDraft(anyList()))
                .willThrow(new AiTimeoutException(TIME_OUT));

        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("image", "image-1.jpg", "dummy".getBytes(StandardCharsets.UTF_8), "image/jpeg")
                .when()
                .post("/ai/post-drafts")
                .then()
                .statusCode(504)
                .body("code", equalTo("SERVER03"));
    }

    @Test
    @DisplayName("AI 게시글 초안 생성 요청 시나리오 테스트 실패 - ai 서버의 실패")
    void makePostDraftByAi_error() {
        given(aiPostDraftClient.requestPostDraft(anyList()))
                .willThrow(new InternalException(SERVER_ERROR));

        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("image", "image-1.jpg", "dummy".getBytes(StandardCharsets.UTF_8), "image/jpeg")
                .when()
                .post("/ai/post-drafts")
                .then()
                .statusCode(500)
                .body("code", equalTo("SERVER01"));
    }
}
