package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.billage.domain.user.dto.UserRequest;
import ktb.billage.domain.user.dto.UserResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "사용자 API")
public interface UserApiDoc {

    @Operation(summary = "회원 가입", description = "로그인 아이디와 비밀번호로 회원 가입합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 가입 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.Id.class))
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 로그인 아이디",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "USER04"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<UserResponse.Id> join(@Valid @RequestBody UserRequest.Join request);

    @Operation(
            summary = "내 프로필 조회",
            description = "내 프로필 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.MyProfile.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "USER01"
                                            }
                                            """
                            ))
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
    ResponseEntity<UserResponse.MyProfile> getMyProfile(@AuthenticatedId Long userId);

    @Operation(
            summary = "웹 푸시 알림 설정 변경",
            description = "내 웹 푸시 알림 허용 여부를 변경합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "웹 푸시 알림 설정 변경 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "USER01"
                                            }
                                            """
                            ))
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
    ResponseEntity<Void> updateWebPushSetting(@RequestBody UserRequest.WebPushEnabled request, @AuthenticatedId Long userId);

    @Operation(
            summary = "푸시 토큰 등록/갱신",
            description = "내 디바이스의 푸시 토큰을 platform + deviceId 기준으로 등록하거나 갱신합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "푸시 토큰 등록/갱신 성공"
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "USER01"
                                            }
                                            """
                            ))
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
    ResponseEntity<Void> updatePushToken(@Valid @RequestBody UserRequest.PushToken request, @AuthenticatedId Long userId);
}
