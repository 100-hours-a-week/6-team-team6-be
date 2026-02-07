package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import ktb.billage.domain.post.dto.PostResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "AI API")
public interface AiApiDoc {

    @Operation(
            summary = "AI 게시글 초안 생성",
            description = "이미지를 기반으로 AI가 게시글 초안을 생성합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 게시글 초안 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.PostDraft.class))
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
                    responseCode = "500",
                    description = "AI 서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "AI Server Error",
                                            value = """
                                                    {
                                                        "code" : "SERVER01"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Image Handling Failed",
                                            value = """
                                                    {
                                                        "code" : "IMAGE05"
                                                    }
                                                    """
                                    )
                            })
            ),
            @ApiResponse(
                    responseCode = "504",
                    description = "AI 서버 타임아웃",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "SERVER03"
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
    ResponseEntity<PostResponse.PostDraft> makePostDraftByAi(
            @RequestPart("image") @NotNull List<@NotNull MultipartFile> image
    );
}
