package ktb.billage.domain.notification.dto;

import java.time.Instant;
import java.util.List;

public record NotificationSummaries(List<NotificationSummary> notificationSummaries, CursorDto cursorDto) {

    public record NotificationSummary(
            Long notificationId,
            Long groupId,
            Long postId,
            Long chatroomId,
            String title,
            String description,
            String type,
            Instant createdAt
    ) {
    }

    public record CursorDto(
            String nextCursor,
            Boolean hasNext
    ) {
    }
}
