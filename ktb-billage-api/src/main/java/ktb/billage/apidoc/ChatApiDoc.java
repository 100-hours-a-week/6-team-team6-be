package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "채팅 API")
public interface ChatApiDoc {

    @Operation(
            summary = "채팅방 생성",
            description = "게시글 기반으로 채팅방을 생성합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "채팅방 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.Id.class))
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "그룹 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "그룹 멤버가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "자기 자신과 채팅 불가",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 채팅방",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT04"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> createChatroom(@PathVariable Long postId, @AuthenticatedId Long userId);

    @Operation(
            summary = "채팅 메시지 조회",
            description = "채팅방 메시지를 커서 기반으로 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메시지 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.Messages.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 커서",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CURSOR01"
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "그룹 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "그룹 멤버가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "채팅방 참여자가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT03"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> getMessages(@PathVariable Long postId,
                                  @PathVariable Long chatroomId,
                                  @AuthenticatedId Long userId,
                                  @RequestParam(required = false) String cursor);

    @Operation(
            summary = "내 게시글 채팅방 목록 조회",
            description = "내가 등록한 게시글의 채팅방 목록을 커서 기반으로 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.ChatroomSummaries.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 커서",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CURSOR01"
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 이미지 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "IMAGE04"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "그룹 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "그룹 멤버가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP02"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> getChatroomsByMyPostId(@PathVariable Long postId,
                                             @AuthenticatedId Long userId,
                                             @RequestParam(required = false) String cursor);

    @Operation(
            summary = "내 전체 채팅방 미확인 메시지 수",
            description = "참여 중인 전체 채팅방의 미확인 메시지 수를 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "미확인 메시지 수 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
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
    ResponseEntity<?> getUnreadMessageCountOnMyAllChatrooms(@AuthenticatedId Long userId);

    @Operation(
            summary = "채팅방 내 게시글 요약 조회",
            description = "채팅방에서 게시글 요약 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 요약 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.PostSummary.class))
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "채팅방 참여자가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT03"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "그룹 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "그룹 멤버가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP02"
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
            )
    })
    ResponseEntity<?> getPostSummaryInChatroom(@PathVariable Long postId,
                                               @PathVariable Long chatroomId,
                                               @AuthenticatedId Long userId);

    @Operation(
            summary = "내 참여 채팅방 목록 조회",
            description = "내가 참여 중인 채팅방 목록을 커서 기반으로 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.ChatroomSummaries.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 커서",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CURSOR01"
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 이미지 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "IMAGE04"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "그룹 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "그룹 멤버가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "GROUP02"
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
            )
    })
    ResponseEntity<?> getMyParticipatingChatrooms(@AuthenticatedId Long userId,
                                                  @RequestParam(required = false) String cursor);

    @Operation(
            summary = "채팅방 기반 게시글 ID 조회",
            description = "채팅방 ID로 게시글 ID를 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 ID 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT02"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "채팅방 참여자가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "CHAT03"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<?> getPostIdByChatroomId(@PathVariable Long chatroomId, @AuthenticatedId Long userId);
}
