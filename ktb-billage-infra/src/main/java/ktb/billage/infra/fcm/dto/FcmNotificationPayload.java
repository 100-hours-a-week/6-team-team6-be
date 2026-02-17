package ktb.billage.infra.fcm.dto;

import java.time.Instant;

public record FcmNotificationPayload(
        String title,
        String body,
        Instant createdAt
) {
}
