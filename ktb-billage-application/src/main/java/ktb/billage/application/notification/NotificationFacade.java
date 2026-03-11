package ktb.billage.application.notification;

import ktb.billage.common.image.ImageService;
import ktb.billage.domain.group.dto.GroupResponse;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.notification.dto.NotificationResponse;
import ktb.billage.domain.notification.dto.NotificationSummaries;
import ktb.billage.domain.notification.service.NotificationService;
import ktb.billage.domain.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationFacade {
    private final NotificationService notificationService;
    private final GroupService groupService;
    private final PostQueryService postQueryService;
    private final ImageService imageService;

    public NotificationResponse.Notifications getMyNotifications(Long userId, String cursor) {
        NotificationSummaries summaries = notificationService.findByUserIdAndCursor(userId, cursor);

        Map<Long, GroupResponse.GroupProfile> groupProfiles = groupService.findGroupProfiles(
                summaries.notificationSummaries().stream()
                        .map(NotificationSummaries.NotificationSummary::groupId)
                        .toList()
        );

        List<Long> postTypeNotificationIds = resolvePostNotificationSummaryIds(summaries.notificationSummaries());
        Map<Long, String> postImageUrls = postQueryService.findPostFirstImageUrls(postTypeNotificationIds);

        return new NotificationResponse.Notifications(
                summaries.notificationSummaries().stream()
                        .map(summary -> new NotificationResponse.NotificationItem(
                                summary.notificationId(),
                                summary.type(),
                                summary.chatroomId(),
                                summary.postId(),
                                imageService.resolveUrl(postImageUrls.get(summary.postId())),
                                summary.title(),
                                summary.groupId(),
                                groupProfiles.get(summary.groupId()).groupName(),
                                summary.description(),
                                summary.createdAt()
                        )).toList(),
                summaries.cursorDto().nextCursor(),
                summaries.cursorDto().hasNext()
        );
    }

    public void deleteNotification(Long userId, Long notificationId) {
        notificationService.softDelete(userId, notificationId);
    }

    private List<Long> resolvePostNotificationSummaryIds(List<NotificationSummaries.NotificationSummary> summaries) {
        return summaries.stream()
                .filter(item -> "POST".equals(item.type()))
                .map(NotificationSummaries.NotificationSummary::postId)
                .toList();
    }
}
