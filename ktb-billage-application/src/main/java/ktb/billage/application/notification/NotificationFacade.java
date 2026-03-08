package ktb.billage.application.notification;

import ktb.billage.domain.group.dto.GroupResponse;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.notification.dto.NotificationResponse;
import ktb.billage.domain.notification.dto.NotificationSummaries;
import ktb.billage.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationFacade {
    private final NotificationService notificationService;
    private final GroupService groupService;

    public NotificationResponse.Notifications getMyNotifications(Long userId, String cursor) {
        NotificationSummaries summaries = notificationService.findByUserIdAndCursor(userId, cursor);

        Map<Long, GroupResponse.GroupProfile> groupProfiles = groupService.findGroupProfiles(
                summaries.notificationSummaries().stream()
                        .map(NotificationSummaries.NotificationSummary::groupId)
                        .toList()
        );

        return new NotificationResponse.Notifications(
                summaries.notificationSummaries().stream()
                        .map(summary -> new NotificationResponse.NotificationItem(
                                summary.notificationId(),
                                summary.type(),
                                summary.chatroomId(),
                                summary.postId(),
                                summary.title(),
                                groupProfiles.get(summary.groupId()).groupName(),
                                summary.description(),
                                summary.createdAt()
                        )).toList(),
                summaries.cursorDto().nextCursor(),
                summaries.cursorDto().hasNext()
        );
    }
}
