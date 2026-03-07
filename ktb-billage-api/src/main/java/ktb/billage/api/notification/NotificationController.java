package ktb.billage.api.notification;

import ktb.billage.apidoc.NotificationApiDoc;
import ktb.billage.domain.notification.dto.NotificationResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApiDoc {

    @GetMapping("/users/me/notifications")
    public ResponseEntity<NotificationResponse.Notifications> getMyNotifications(
            @AuthenticatedId Long userId,
            @RequestParam(required = false) String cursor
    ) {
        List<NotificationResponse.NotificationItem> notifications = resolveMockNotifications(cursor);
        NotificationResponse.Notifications response = new NotificationResponse.Notifications(
                notifications,
                "a1b2c3",
                true
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/me/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticatedId Long userId,
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.noContent().build();
    }

    private List<NotificationResponse.NotificationItem> resolveMockNotifications(
            String cursor
    ) {
        if (cursor == null || cursor.isBlank()) {
            return List.of(
                    new NotificationResponse.NotificationItem(
                            101L,
                            "CHATROOM",
                            3L,
                            null,
                            "화난 라이언",
                            "카카오테크 부트캠프",
                            "새로운 채팅이 도착했어요.",
                            Instant.parse("2026-01-12T09:41:20.123456Z")
                    ),
                    new NotificationResponse.NotificationItem(
                            102L,
                            "POST",
                            null,
                            12L,
                            "노트북 물품 등록",
                            "카카오테크 부트캠프",
                            "맥북 노트북 13 air",
                            Instant.parse("2026-01-11T16:23:19.123456Z")
                    )
            );
        }

        return List.of(
                new NotificationResponse.NotificationItem(
                        103L,
                        "CHATROOM",
                        8L,
                        null,
                        "춘식이 스탠드",
                        "카카오테크 부트캠프",
                        "새로운 채팅이 도착했어요.",
                        Instant.parse("2026-01-10T11:15:00.123456Z")
                )
        );
    }
}
