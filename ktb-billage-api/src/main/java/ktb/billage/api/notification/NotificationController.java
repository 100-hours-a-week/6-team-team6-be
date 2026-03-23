package ktb.billage.api.notification;

import ktb.billage.apidoc.NotificationApiDoc;
import ktb.billage.application.notification.NotificationFacade;
import ktb.billage.domain.notification.dto.NotificationResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApiDoc {
    private final NotificationFacade notificationFacade;

    @GetMapping("/users/me/notifications")
    public ResponseEntity<NotificationResponse.Notifications> getMyNotifications(
            @AuthenticatedId Long userId,
            @RequestParam(required = false) String cursor
    ) {
        var response = notificationFacade.getMyNotifications(userId, cursor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/me/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticatedId Long userId,
            @PathVariable Long notificationId
    ) {
        notificationFacade.deleteNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
