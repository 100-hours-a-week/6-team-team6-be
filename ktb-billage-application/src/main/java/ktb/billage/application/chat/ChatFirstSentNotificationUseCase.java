package ktb.billage.application.chat;

import ktb.billage.domain.membership.dto.MembershipProfile;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.notification.Type;
import ktb.billage.domain.notification.service.NotificationService;
import ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFirstSentNotificationUseCase {
    private static final String DESCRIPTION = "새로운 채팅이 도착했습니다.";

    private final MembershipService membershipService;
    private final NotificationService notificationService;

    public void handle(BuyerFirstMessageSentEvent event) {
        MembershipProfile buyerMembershipProfile = membershipService.findMembershipProfile(event.buyerMembershipId());

        notificationService.save(new Notification(
                event.sellerUserId(),
                buyerMembershipProfile.groupId(),
                null,
                event.chatroomId(),
                buyerMembershipProfile.nickname(),
                DESCRIPTION,
                Type.CHATROOM
        ));
    }
}
