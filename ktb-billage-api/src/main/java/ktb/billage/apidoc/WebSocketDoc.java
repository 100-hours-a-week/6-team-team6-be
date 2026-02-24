package ktb.billage.apidoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "WebSocket API", description = "STOMP over WebSocket 채팅 문서")
@RestController
@RequestMapping("/docs/websocket")
public class WebSocketDoc {

    @Operation(
            summary = "WebSocket 연결 및 인증",
            description = """
                    WebSocket 엔드포인트: `/ws`
                    
                    인증:
                    - STOMP CONNECT 프레임의 `Authorization` 헤더에 JWT를 전달합니다.
                    - 예: `Authorization: Bearer {accessToken}`
                    
                    기본 흐름:
                    1. `/ws`로 WebSocket 연결
                    2. STOMP CONNECT (Authorization 포함)
                    3. 구독: `/topic/chatrooms/{chatroomId}` 또는 `/user/queue/chat-inbox`
                    4. 전송: `/app/chat/send`
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "연결 정보 확인용 문서",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "websocketEndpoint": "/ws",
                                              "stompSendDestination": "/app/chat/send",
                                              "stompSubscribeDestinations": [
                                                "/topic/chatrooms/{chatroomId}",
                                                "/user/queue/chat-inbox"
                                              ],
                                              "authHeader": "Authorization: Bearer {accessToken}"
                                            }
                                            """
                            ))
            )
    })
    @GetMapping("/connection")
    public ResponseEntity<?> connection() {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "STOMP 메시지 전송: /app/chat/send",
            description = """
                    클라이언트가 채팅 메시지를 보낼 때 사용하는 STOMP SEND 목적지입니다.
                    
                    SEND destination:
                    - `/app/chat/send`
                    
                    요청 payload 필드:
                    - `chatroomId` (Long): 채팅방 ID
                    - `membershipId` (Long): 보낸 사용자 멤버십 ID
                    - `message` (String): 전송할 메시지
                    """,
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "요청 메시지 예시",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "ChatSendRequest",
                                    value = """
                                            {
                                              "chatroomId": 1001,
                                              "membershipId": 501,
                                              "message": "안녕하세요. 거래 가능할까요?"
                                            }
                                            """
                            ))
            )
    })
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage() {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "구독 경로: /topic/chatrooms/{chatroomId}",
            description = """
                    특정 채팅방 공개 스트림 구독 경로입니다.
                    
                    SUBSCRIBE destination:
                    - `/topic/chatrooms/{chatroomId}`
                    
                    서버 응답 payload (ChatSendAckResponse):
                    - `chatroomId` (Long)
                    - `membershipId` (Long)
                    - `messageId` (String)
                    - `messageContent` (String)
                    - `createdAt` (OffsetDateTime, date-time)
                    """,
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 구독 응답 예시",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "ChatroomTopicMessage",
                                    value = """
                                            {
                                              "chatroomId": 1001,
                                              "membershipId": 501,
                                              "messageId": "msg_20260217_0001",
                                              "messageContent": "안녕하세요. 거래 가능할까요?",
                                              "createdAt": "2026-02-17T17:30:00Z"
                                            }
                                            """
                            ))
            )
    })
    @GetMapping("/subscribe/chatroom")
    public ResponseEntity<?> subscribeChatroom() {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "구독 경로: /user/queue/chat-inbox",
            description = """
                    사용자 개인 inbox 구독 경로입니다.
                    
                    SUBSCRIBE destination:
                    - `/user/queue/chat-inbox`
                    
                    주의:
                    - 클라이언트는 `/user/{userId}/...` 형태를 직접 구독하지 않습니다.
                    - 사용자 식별은 CONNECT 프레임의 JWT 기준으로 서버가 처리합니다.
                    
                    응답 payload 구조는 `ChatSendAckResponse`와 동일합니다.
                    """,
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 inbox 구독 응답 예시",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "UserInboxMessage",
                                    value = """
                                            {
                                              "chatroomId": 1001,
                                              "membershipId": 777,
                                              "messageId": "msg_20260217_0002",
                                              "messageContent": "네, 오늘 저녁 가능합니다.",
                                              "createdAt": "2026-02-17T17:31:15Z"
                                            }
                                            """
                            ))
            )
    })
    @GetMapping("/subscribe/inbox")
    public ResponseEntity<?> subscribeInbox() {
        return ResponseEntity.ok().build();
    }
}
