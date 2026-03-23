package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.billage.domain.keywordsubscription.dto.KeywordSubscriptionRequest;
import ktb.billage.domain.keywordsubscription.dto.KeywordSubscriptionResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Tag(name = "키워드 구독 API")
public interface KeywordSubscriptionApiDoc {

    @Operation(
            summary = "키워드 구독 등록",
            description = "그룹 내 내 키워드 구독을 등록합니다.",
            security = {@SecurityRequirement(name = "Bearer Auth")},
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KeywordSubscriptionRequest.Create.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "keyword" : "노트북"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "키워드 구독 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KeywordSubscriptionResponse.Id.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "keywordSubscriptionId" : 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "키워드 형식 오류",
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
                    responseCode = "401",
                    description = "인증 실패",
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
                    responseCode = "403",
                    description = "그룹원이 아님",
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
                    description = "존재하지 않는 그룹",
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
                    responseCode = "409",
                    description = "이미 등록된 키워드 또는 등록 가능 개수 초과",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "중복 키워드",
                                            value = """
                                                    {
                                                      "code" : "KEYWORD01"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "개수 초과",
                                            value = """
                                                    {
                                                      "code" : "KEYWORD02"
                                                    }
                                                    """
                                    )
                            })
            )
    })
    ResponseEntity<KeywordSubscriptionResponse.Id> createKeywordSubscription(
            @PathVariable Long groupId,
            @org.springframework.web.bind.annotation.RequestBody KeywordSubscriptionRequest.Create request,
            @AuthenticatedId Long userId
    );

    @Operation(
            summary = "키워드 구독 삭제",
            description = "그룹 내 내 키워드 구독 하나를 삭제합니다.",
            security = {@SecurityRequirement(name = "Bearer Auth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "키워드 구독 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
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
                    responseCode = "403",
                    description = "그룹원이 아니거나 본인 구독이 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "그룹원이 아님",
                                            value = """
                                                    {
                                                      "code" : "GROUP02"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "본인 구독이 아님",
                                            value = """
                                                    {
                                                      "code" : "KEYWORD05"
                                                    }
                                                    """
                                    )
                            })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "그룹이 없거나 구독이 없거나 이미 삭제됨",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "그룹 없음",
                                            value = """
                                                    {
                                                      "code" : "GROUP01"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "구독 없음",
                                            value = """
                                                    {
                                                      "code" : "KEYWORD04"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 삭제됨",
                                            value = """
                                                    {
                                                      "code" : "KEYWORD03"
                                                    }
                                                    """
                                    )
                            })
            )
    })
    ResponseEntity<Void> deleteKeywordSubscription(
            @PathVariable Long groupId,
            @PathVariable Long keywordSubscriptionId,
            @AuthenticatedId Long userId
    );

    @Operation(
            summary = "키워드 구독 조회",
            description = "그룹 내 내 키워드 구독 목록을 조회합니다.",
            security = {@SecurityRequirement(name = "Bearer Auth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "키워드 구독 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KeywordSubscriptionResponse.Summaries.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "keywordSubscriptions" : [
                                                {
                                                  "keywordSubscriptionId" : 1,
                                                  "keyword" : "노트북",
                                                  "createdAt" : "2026-01-12T09:41:20.123456Z"
                                                },
                                                {
                                                  "keywordSubscriptionId" : 2,
                                                  "keyword" : "핸드폰",
                                                  "createdAt" : "2026-01-11T16:23:19.123456Z"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
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
                    responseCode = "403",
                    description = "그룹원이 아님",
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
                    description = "존재하지 않는 그룹",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code" : "GROUP01"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<KeywordSubscriptionResponse.Summaries> getMyKeywordSubscriptions(
            @PathVariable Long groupId,
            @AuthenticatedId Long userId
    );
}
