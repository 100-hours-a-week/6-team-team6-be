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
import jakarta.validation.Valid;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "게시글 API")
public interface PostApiDoc {

    @Operation(
            summary = "게시글 생성",
            description = "그룹 내 게시글을 생성합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.Id.class))
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
    ResponseEntity<PostResponse.Id> createPost(@PathVariable Long groupId,
                                               @Valid @RequestBody PostRequest.Create request,
                                               @AuthenticatedId Long userId);

    @Operation(
            summary = "게시글 수정",
            description = "게시글을 수정합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.Id.class))
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
                    responseCode = "403",
                    description = "게시글 소유자가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST02"
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
    ResponseEntity<PostResponse.Id> modifyPost(@PathVariable Long groupId,
                                               @PathVariable Long postId,
                                               @Valid @RequestBody PostRequest.Update request,
                                               @AuthenticatedId Long userId);

    @Operation(
            summary = "게시글 AI 검증",
            description = "기존 게시글의 제목, 내용, 이미지 정보로 AI 검증을 수행합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 AI 검증 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "AI 검증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
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
    ResponseEntity<Void> checkPostContent(@PathVariable Long postId,
                                          @AuthenticatedId Long userId);

    @Operation(
            summary = "게시글 대여 상태 변경",
            description = "게시글의 대여 상태를 변경합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "대여 상태 변경 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.ChangedStatus.class))
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
                    responseCode = "403",
                    description = "게시글 소유자가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST02"
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
    ResponseEntity<PostResponse.ChangedStatus> changeRentalStatus(@PathVariable Long groupId,
                                                                   @PathVariable Long postId,
                                                                   @RequestBody PostRequest.Change request,
                                                                   @AuthenticatedId Long userId);

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 삭제 성공"
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
                    responseCode = "403",
                    description = "게시글 소유자가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST02"
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
    ResponseEntity<Void> deletePost(@PathVariable Long groupId,
                                    @PathVariable Long postId,
                                    @AuthenticatedId Long userId);

    @Operation(
            summary = "게시글 목록 조회",
            description = """
                    키워드와 커서로 게시글 목록을 조회합니다.
                    
                    - `cursor`가 없는 첫 조회에서는 추천 게시글이 함께 섞여 내려갈 수 있습니다.
                    - 추천 게시글은 응답 내 `feedItemType` 필드로 구분하며 값은 `BASIC` 또는 `RECOMMENDATION` 입니다.
                    - 첫 조회에서만 추천 게시글이 최대 4개까지 5개 단위 블록마다 1개씩 랜덤한 위치에 포함될 수 있습니다.
                    - `cursor`가 있는 후속 조회에서는 일반 게시글만 반환됩니다.
                    """,
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.Summaries.class),
                            examples = {
                                    @ExampleObject(
                                            name = "첫 조회 응답 예시",
                                            value = """
                                                    {
                                                      "summaries": [
                                                        {
                                                          "postId": 301,
                                                          "postTitle": "전동 드릴 빌려요",
                                                          "postImageId": 1201,
                                                          "postFirstImageUrl": "https://cdn.example.com/posts/301.jpg",
                                                          "rentalFee": 3000,
                                                          "feeUnit": "HOUR",
                                                          "rentalStatus": "AVAILABLE",
                                                          "updatedAt": "2026-03-19T10:15:30Z",
                                                          "feedItemType": "BASIC"
                                                        },
                                                        {
                                                          "postId": 998,
                                                          "postTitle": "이런 물품도 필요하신가요?",
                                                          "postImageId": null,
                                                          "postFirstImageUrl": "https://cdn.example.com/posts/998.jpg",
                                                          "rentalFee": 5000,
                                                          "feeUnit": "DAY",
                                                          "rentalStatus": null,
                                                          "updatedAt": null,
                                                          "feedItemType": "RECOMMENDATION"
                                                        }
                                                      ],
                                                      "nextCursor": "eyJ0aW1lIjoiMjAyNi0wMy0xOVQxMDoxMDozMFoiLCJpZCI6MzAxfQ==",
                                                      "hasNextPage": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "후속 조회 응답 예시",
                                            value = """
                                                    {
                                                      "summaries": [
                                                        {
                                                          "postId": 280,
                                                          "postTitle": "캠핑 의자 빌려드립니다",
                                                          "postImageId": 1110,
                                                          "postFirstImageUrl": "https://cdn.example.com/posts/280.jpg",
                                                          "rentalFee": 2000,
                                                          "feeUnit": "DAY",
                                                          "rentalStatus": "AVAILABLE",
                                                          "updatedAt": "2026-03-18T08:00:00Z",
                                                          "feedItemType": "BASIC"
                                                        }
                                                      ],
                                                      "nextCursor": "eyJ0aW1lIjoiMjAyNi0wMy0xOFQwODowMDowMFoiLCJpZCI6MjgwfQ==",
                                                      "hasNextPage": true
                                                    }
                                                    """
                                    )
                            }
                    )
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
    ResponseEntity<PostResponse.Summaries> getPostsByKeywordAndCursor(@PathVariable Long groupId,
                                                                      @AuthenticatedId Long userId,
                                                                      @RequestParam(required = false) String query,
                                                                      @RequestParam(required = false) String cursor);

    @Operation(
            summary = "내 게시글 목록 조회",
            description = "내가 작성한 게시글 목록을 커서로 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 게시글 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.MySummaries.class))
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
    ResponseEntity<?> getMyPostsByCursor(@AuthenticatedId Long userId,
                                         @RequestParam(required = false) String cursor);

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 상세 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.Detail.class))
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
    ResponseEntity<?> getPost(@PathVariable Long groupId,
                              @PathVariable Long postId,
                              @AuthenticatedId Long userId);

    @Operation(
            summary = "게시글 추천 조회",
            description = "AI 서버가 추천한 유사 게시글 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "추천 게시글 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.Recommendations.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "AI 추천 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "AI01"
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
                    description = "게시글 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code" : "POST01"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<PostResponse.Recommendations> getRecommendations(@PathVariable Long postId,
                                                                    @AuthenticatedId Long userId);
}
