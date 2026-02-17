package ktb.billage.infra.fcm.dto;

public record FcmDataPayload(
        Long chatroomId,
        String messageId,
        Long membershipId
) {
}
