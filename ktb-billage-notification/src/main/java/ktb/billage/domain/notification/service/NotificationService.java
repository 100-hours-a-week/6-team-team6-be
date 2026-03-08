package ktb.billage.domain.notification.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.common.exception.NotificationException;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.notification.NotificationRepository;
import ktb.billage.domain.notification.dto.NotificationSummaries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.NOTIFICATION_ALREAY_DELETED;
import static ktb.billage.common.exception.ExceptionCode.NOTIFICATION_NOT_FOUND;
import static ktb.billage.common.exception.ExceptionCode.NOTIFICATION_NOT_OWNED;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    private final CursorCodec cursorCodec;

    public NotificationSummaries findByUserIdAndCursor(Long userId, String cursor) {
        List<Notification> notifications;
        if (cursor == null) {
            notifications = notificationRepository.findTop21ByUserIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(userId);
        } else {
            CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
            notifications = notificationRepository.findNextPageByUserIdAndDeletedAtIsNullWithCursorOrderByCreatedAtDescIdDesc(userId, decoded.time(), decoded.id());
        }

        return buildSummaries(notifications);
    }

    private NotificationSummaries buildSummaries(List<Notification> notifications) {
        var cursorDto = encodeCursor(notifications);

        var subNotifications  = cursorDto.hasNext() ? notifications.subList(0, 20) : notifications;

        return new NotificationSummaries(
                subNotifications.stream()
                        .map(notification -> new NotificationSummaries.NotificationSummary(
                                notification.getId(),
                                notification.getGroupId(),
                                notification.getPostId(),
                                notification.getChatroomId(),
                                notification.getTitle(),
                                notification.getDescription(),
                                notification.getType().name(),
                                notification.getCreatedAt()
                        ))
                        .toList(),
                cursorDto
        );
    }

    private NotificationSummaries.CursorDto encodeCursor(List<Notification> notifications) {
        boolean hasNextPage = notifications.size() > 20;

        if (hasNextPage) {
            var pagedNotification = notifications.get(19);
            String encode = cursorCodec.encode(pagedNotification.getCreatedAt(), pagedNotification.getId());
            return new NotificationSummaries.CursorDto(encode, true);
        } else {
            return new NotificationSummaries.CursorDto(null, false);
        }
    }

    public void softDelete(Long userId, Long notificationId) {
        Notification notification = findNotificationWithDeletion(notificationId);

        if (!notification.isOwned(userId)) {
            throw new NotificationException(NOTIFICATION_NOT_OWNED);
        }

        if (notification.isDeleted()) {
            throw new NotificationException(NOTIFICATION_ALREAY_DELETED);
        }

        notification.delete(Instant.now());
    }

    private Notification findNotificationWithDeletion(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NOTIFICATION_NOT_FOUND));
    }
}
