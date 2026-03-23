package ktb.billage.application.chat;

import ktb.billage.domain.membership.dto.MembershipProfile;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.notification.Type;
import ktb.billage.domain.notification.service.NotificationService;
import ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatFirstSentNotificationUseCaseUnitTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChatFirstSentNotificationUseCase useCase;

    @Test
    @DisplayName("구매자의 첫 메시지 이벤트를 받으면 판매자 알림 1건을 저장한다")
    void handle_ShouldSaveChatroomNotificationForSeller() {
        BuyerFirstMessageSentEvent event = new BuyerFirstMessageSentEvent(11L, 22L, 33L);
        MembershipProfile buyerMembershipProfile = new MembershipProfile(22L, 44L, 55L, "구매자닉네임");

        when(membershipService.findMembershipProfile(22L)).thenReturn(buyerMembershipProfile);

        useCase.handle(event);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).save(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertThat(notification.getUserId()).isEqualTo(33L);
        assertThat(notification.getGroupId()).isEqualTo(44L);
        assertThat(notification.getPostId()).isNull();
        assertThat(notification.getChatroomId()).isEqualTo(11L);
        assertThat(notification.getTitle()).isEqualTo("구매자닉네임");
        assertThat(notification.getDescription()).isEqualTo("새로운 채팅이 도착했습니다.");
        assertThat(notification.getType()).isEqualTo(Type.CHATROOM);
    }
}
