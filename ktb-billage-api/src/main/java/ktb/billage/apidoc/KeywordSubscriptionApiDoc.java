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
                                                  "keyword" : "노트북"
                                                },
                                                {
                                                  "keywordSubscriptionId" : 2,
                                                  "keyword" : "핸드폰"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<KeywordSubscriptionResponse.Summaries> getMyKeywordSubscriptions(
            @PathVariable Long groupId,
            @AuthenticatedId Long userId
    );
}
