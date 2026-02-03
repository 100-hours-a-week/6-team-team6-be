package ktb.billage.websocket.dto;

import java.time.Instant;

public record ChatSendAckResponse(
        Long chatroomId,
        Long membershipId,
        String messageId,
        String messageContent,
        Instant createdAt
){
}
