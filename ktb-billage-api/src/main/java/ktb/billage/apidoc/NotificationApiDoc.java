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
import ktb.billage.domain.notification.dto.NotificationResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "알림 API")
public interface NotificationApiDoc {

    @Operation(
            summary = "알림 목록 조회",
            description = "내 알림 목록을 커서 기반으로 조회합니다.",
            security = {@SecurityRequirement(name = "Bearer Auth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponse.Notifications.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "notifications" : [
                                                {
                                                  "notificationId" : 101,
                                                  "type" : "CHATROOM",
                                                  "chatroomId" : 3,
                                                  "postId" : null,
                                                  "title" : "화난 라이언",
                                                  "groupName" : "카카오테크 부트캠프",
                                                  "description" : "새로운 채팅이 도착했어요.",
                                                  "createdAt" : "2026-01-12T09:41:20.123456Z"
                                                },
                                                {
                                                  "notificationId" : 102,
                                                  "type" : "POST",
                                                  "chatroomId" : null,
                                                  "postId" : 12,
                                                  "title" : "노트북 물품 등록",
                                                  "groupName" : "카카오테크 부트캠프",
                                                  "description" : "맥북 노트북 13 air",
                                                  "createdAt" : "2026-01-11T16:23:19.123456Z"
                                                }
                                              ],
                                              "nextCursor" : "a1b2c3",
                                              "hasNext" : true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 커서",
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
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code" : "AUTH02"
                                            }
                                            """
                            ))
            )
    })
    ResponseEntity<NotificationResponse.Notifications> getMyNotifications(
            @AuthenticatedId Long userId,
            @Parameter(description = "커서 기반 페이지네이션용 커서", required = false)
            @RequestParam(required = false) String cursor
    );

    @Operation(
            summary = "알림 단일 삭제",
            description = "내 알림 하나를 삭제합니다.",
            security = {@SecurityRequirement(name = "Bearer Auth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "알림 삭제 성공"
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
                    description = "본인 알림이 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code" : "NOTIFICATION03"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림이 존재하지 않거나 이미 삭제됨",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 알림",
                                            value = """
                                                    {
                                                      "code" : "NOTIFICATION01"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 삭제된 알림",
                                            value = """
                                                    {
                                                      "code" : "NOTIFICATION02"
                                                    }
                                                    """
                                    )
                            })
            )
    })
    ResponseEntity<Void> deleteNotification(
            @AuthenticatedId Long userId,
            @PathVariable Long notificationId
    );
}
