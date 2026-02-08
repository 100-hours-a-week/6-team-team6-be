package ktb.billage.api.image;

import io.restassured.RestAssured;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.AcceptanceTest;
import ktb.billage.support.AcceptanceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;

@AcceptanceTest
class ImageAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private Fixtures fixtures;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = fixtures.유저_생성();
        accessToken = fixtures.토큰_생성(user);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("이미지 업로드 성공")
    void upload_image_success(int size) throws Exception {
        byte[] large = new byte[size * 1024 * 1024];
        Path tempFile = Files.createTempFile("billage-large-", ".jpg");
        Files.write(tempFile, large);
        try {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .multiPart("image", tempFile.toFile(), "image/jpeg")
                    .when()
                    .post("/images")
                    .then()
                    .statusCode(200);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("이미지 업로드 실패 시나리오 - 빈 파일")
    void upload_image_fail_empty() {
        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .multiPart("image", "empty.jpg", new byte[0], "image/jpeg")
                .when()
                .post("/images")
                .then()
                .statusCode(400)
                .body("code", equalTo("IMAGE01"));
    }

    @Test
    @DisplayName("이미지 업로드 실패 시나리오 - 지원하지 않는 타입")
    void upload_image_fail_unsupported_type() {
        RestAssured.given()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .multiPart("image", "text.txt", "dummy".getBytes(StandardCharsets.UTF_8), "text/plain")
                .when()
                .post("/images")
                .then()
                .statusCode(415)
                .body("code", equalTo("IMAGE02"));
    }

    @Test
    @DisplayName("이미지 업로드 실패 시나리오 - 용량 초과")
    void upload_image_fail_size_limit() throws Exception {
        byte[] large = new byte[10 * 1024 * 1024 + 1];
        Path tempFile = Files.createTempFile("billage-large-", ".jpg");
        Files.write(tempFile, large);
        try {
            RestAssured.given()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                    .multiPart("image", tempFile.toFile(), "image/jpeg")
                    .when()
                    .post("/images")
                    .then()
                    .statusCode(413)
                    .body("code", equalTo("IMAGE03"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
