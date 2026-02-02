package ktb.billage.api.ai;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import ktb.billage.common.exception.AiTimeoutException;
import ktb.billage.domain.post.ai.AiPostDraftClient;
import ktb.billage.domain.post.dto.PostResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.Import;
import ktb.billage.web.common.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static ktb.billage.common.exception.ExceptionCode.TIME_OUT;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
@Import(GlobalExceptionHandler.class)
class AiAcceptanceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private AiPostDraftClient aiPostDraftClient;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    @DisplayName("AI 게시글 초안 생성 요청이 정상 응답을 반환한다")
    void makePostDraftByAi_success() {
        PostResponse.PostDraft draft = new PostResponse.PostDraft(
                "드릴 대여",
                "생활용 드릴입니다.",
                new BigDecimal("5000"),
                "DAY"
        );
        given(aiPostDraftClient.requestPostDraft(anyList())).willReturn(draft);

        given()
                .multiPart("images", "image-1.jpg", "dummy".getBytes(StandardCharsets.UTF_8), "image/jpeg")
                .when()
                .post("/ai/post-drafts")
                .then()
                .statusCode(200)
                .body("title", Matchers.equalTo("드릴 대여"))
                .body("content", Matchers.equalTo("생활용 드릴입니다."))
                .body("rentalFee", Matchers.equalTo(5000))
                .body("feeUnit", Matchers.equalTo("DAY"));
    }

    @Test
    @DisplayName("AI 게시글 초안 생성 요청이 타임아웃으로 실패한다")
    void makePostDraftByAi_timeout() {
        given(aiPostDraftClient.requestPostDraft(anyList()))
                .willThrow(new AiTimeoutException(TIME_OUT));

        given()
                .multiPart("images", "image-1.jpg", "dummy".getBytes(StandardCharsets.UTF_8), "image/jpeg")
                .when()
                .post("/ai/post-drafts")
                .then()
                .statusCode(504)
                .body("code", Matchers.equalTo("SERVER03"));
    }
}
