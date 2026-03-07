package ktb.billage.domain.notification.dto;

import java.time.Instant;
import java.util.List;

public class NotificationResponse {

    public record Notifications(
            List<NotificationItem> notifications,
            String nextCursor,
            Boolean hasNext
    ) {
    }

    public record NotificationItem(
            Long notificationId,
            String type,
            Long chatroomId,
            Long postId,
            String title,
            String groupName,
            String description,
            Instant createdAt
    ) {
    }
}
