package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "예외 코드")
public interface ErrorCodeApiDoc {

    @Operation(
            summary = "예외 코드 목록",
            description = "도메인별 예외 코드와 사유를 정리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "1",
                    description = "auth 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "AUTH01": "아이디가 존재하지 않거나 비밀번호가 일치하지 않음",
                                                "AUTH02": "인증 토큰이 요청에 없음(Authorization 헤더 없음)"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "2",
                    description = "token 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "TOKEN01": "액세스 토큰이 유효하지 않음(형식/Bearer prefix 오류 등)",
                                                "TOKEN02": "리프레시 토큰이 유효하지 않음(저장소에 없음)",
                                                "TOKEN03": "리프레시 토큰이 만료됨",
                                                "TOKEN04": "액세스 토큰이 만료됨"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "3",
                    description = "user 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "USER01": "사용자를 찾을 수 없음",
                                                "USER02": "로그인 ID 형식이 유효하지 않음",
                                                "USER03": "닉네임 형식이 유효하지 않음",
                                                "USER04": "로그인 ID가 중복됨"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "4",
                    description = "group 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "GROUP01": "그룹을 찾을 수 없음",
                                                "GROUP02": "그룹 멤버가 아니거나 소유자가 아님",
                                                "GROUP03": "이미 그룹 멤버임",
                                                "GROUP04": "그룹 최대 인원 초과",
                                                "GROUP05": "사용자 그룹 가입 제한 초과",
                                                "GROUP06": "유효하지 않은 초대장"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "5",
                    description = "post 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "POST01": "게시글을 찾을 수 없음",
                                                "POST02": "게시글 소유자가 아님"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "6",
                    description = "chat 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "CHAT01": "자기 자신과의 채팅은 불가",
                                                "CHAT02": "존재하지 않는 채팅방",
                                                "CHAT03": "채팅방에 참여중이지 않음(권한 없음)"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "7",
                    description = "image 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "IMAGE01": "이미지 파일이 없거나 비어 있음",
                                                "IMAGE02": "지원하지 않는 이미지 타입",
                                                "IMAGE03": "이미지 용량 제한 초과(5MB 초과)",
                                                "IMAGE04": "게시글 이미지 정보를 찾을 수 없음",
                                                "IMAGE05": "이미지 처리 실패"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "8",
                    description = "websocket 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "WS01": "헤더에 JWT 토큰 없음",
                                                "WS02": "이미 연결된 상태에서 WS CONNECT 재시도"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "9",
                    description = "common 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "CURSOR01": "커서 형식이 유효하지 않음",
                                                "PARAMETER01": "요청 파라미터 검증 실패"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "10",
                    description = "application 예외 코드",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "SERVER01": "서버 내부 오류",
                                                "SERVER02": "요청 api에대한 rest 메서드 틀림",
                                                "SERVER03": "외부 api 타임아웃(ai 기능)"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> listErrorCodes();
}
