package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import ktb.billage.domain.auth.dto.AuthRequest;
import ktb.billage.web.common.annotation.AuthenticatedId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;

import java.util.Map;

@Tag(name = "인증 API")
public interface AuthApiDoc {

    @Operation(summary = "로그인", description = "로그인 아이디와 비밀번호를 입력하여 인증 요청",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = AuthRequest.Login.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "loginId" : "testlogin",
                                                "password" : "qwer1234Q!"
                                            }
                                            """
                            ))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "refreshToken, XSRF-TOKEN 쿠키가 각각 응답 헤더로 내려옵니다.",
                            schema = @Schema(type = "string",
                                    example = "refreshToken=abcd1234~~; Path=/auth; HttpOnly; SameSite=None, " +
                                            "\nXSRF-TOKEN=abcd1234~~; Path=/; SameSite=None")
                    ),
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "accessToken" : "abcd1234~~",
                                                "userId" : 1
                                            }
                                            """
                    ))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                        "code" : "AUTH01"
                                    }
                                    """
                    ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "PARAMETER01"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> login(@RequestBody AuthRequest.Login request, HttpServletResponse response);

    @Operation(
            summary = "토큰 재발급",
            description = "refreshToken 쿠키를 사용해 accessToken을 재발급합니다. 요청 시 X-XSRF-TOKEN 헤더와 XSRF-TOKEN 쿠키를 함께 전달해야 합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "refreshToken, XSRF-TOKEN 쿠키가 각각 응답 헤더로 내려옵니다.",
                            schema = @Schema(type = "string",
                                    example = "refreshToken=abcd1234~~; Path=/auth; HttpOnly; SameSite=None, XSRF-TOKEN=abcd1234~~; Path=/; SameSite=None")
                    ),
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "accessToken" : "abcd1234~~"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 refreshToken",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "TOKEN02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "만료된 refreshToken",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "TOKEN03"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "PARAMETER01"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> reissue(
            @Parameter(in = ParameterIn.COOKIE, name = "refreshToken", required = true, description = "refreshToken 쿠키")
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    );

    @Operation(
            summary = "로그아웃",
            description = "현재 사용자 토큰을 폐기합니다. 요청 시 X-XSRF-TOKEN 헤더와 XSRF-TOKEN 쿠키를 함께 전달해야 합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "로그아웃 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "AUTH02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 토큰",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "TOKEN01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "만료된 토큰",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "TOKEN04"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> logout(
            @AuthenticatedId Long userId,
            @Parameter(in = ParameterIn.COOKIE, name = "refreshToken", required = false, description = "refreshToken 쿠키")
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    );
}
