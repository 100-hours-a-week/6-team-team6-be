package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "이미지 API")
public interface ImageApiDoc {

    @Operation(
            summary = "이미지 업로드",
            description = "이미지 파일을 업로드합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업로드 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 이미지",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "IMAGE01"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "이미지 용량 초과",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "IMAGE03"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "지원하지 않는 이미지 타입",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "IMAGE02"
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
    ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile image);
}
