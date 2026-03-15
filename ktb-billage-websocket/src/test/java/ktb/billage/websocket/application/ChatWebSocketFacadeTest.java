package ktb.billage.websocket.application;

import ktb.billage.domain.chat.dto.PartnerProfile;
import ktb.billage.domain.chat.service.ChatMessageCommandService;
import ktb.billage.domain.chat.service.ChatroomQueryService;
import ktb.billage.domain.group.dto.GroupResponse;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent;
import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatEventPublisher;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketFacadeTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private ChatroomQueryService chatroomQueryService;

    @Mock
    private GroupService groupService;

    @Mock
    private ChatMessageCommandService chatMessageCommandService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ChatEventPublisher chatEventPublisher;

    @InjectMocks
    private ChatWebSocketFacade chatWebSocketFacade;

    @Test
    @DisplayName("첫 메시지를 보내면 채팅 첫 발송 이벤트를 발행하고 ACK 를 반환한다")
    void sendMessage_ShouldResolveReceiverUserIdAndReturnChatSendResult() {
        Long chatroomId = 1L;
        Long sendUserId = 10L;
        Long sendMembershipId = 100L;
        Long receiveMembershipId = 200L;
        Long receiveUserId = 20L;
        String message = "test message";
        String clientMessageId = "test-client-message-id";

        when(chatroomQueryService.findPartnerProfile(chatroomId, sendMembershipId))
                .thenReturn(new PartnerProfile(receiveMembershipId, "partner", false));
        when(chatroomQueryService.isEmptyMessageChatroom(chatroomId)).thenReturn(true);
        when(membershipService.findUserIdByMembershipId(receiveMembershipId))
                .thenReturn(receiveUserId);
        when(chatMessageCommandService.sendMessage(eq(chatroomId), eq(sendMembershipId), eq(message), any(Instant.class), any(String.class)))
                .thenReturn(999L);
        when(groupService.findGroupProfileByMembershipId(sendMembershipId)).thenReturn(new GroupResponse.GroupProfile(888L, "test group", "group-cover.url"));

        ChatSendAckResponse ack = chatWebSocketFacade.sendMessage(chatroomId, sendUserId, sendMembershipId, message, clientMessageId);

        assertThat(ack.chatroomId()).isEqualTo(chatroomId);
        assertThat(ack.membershipId()).isEqualTo(sendMembershipId);
        assertThat(ack.messageId()).isEqualTo("999");
        assertThat(ack.messageContent()).isEqualTo(message);
        assertThat(ack.createdAt()).isNotNull();
        assertThat(ack.clientMessageId()).isEqualTo(clientMessageId);

        verify(membershipService).validateMembershipOwner(sendUserId, sendMembershipId);
        verify(chatroomQueryService).validateParticipating(chatroomId, sendMembershipId);
        verify(chatroomQueryService).findPartnerProfile(chatroomId, sendMembershipId);
        verify(membershipService).findUserIdByMembershipId(receiveMembershipId);
        verify(chatEventPublisher).publishFirstMessageSent(new BuyerFirstMessageSentEvent(chatroomId, sendMembershipId, receiveUserId));
        verify(chatMessageCommandService).sendMessage(chatroomId, sendMembershipId, message, ack.createdAt(), clientMessageId);
        verify(eventPublisher).publishEvent(new ChatInboxSendEvent(receiveUserId, ack));
    }

    @Test
    @DisplayName("첫 메시지가 아니면 채팅 첫 발송 이벤트를 발행하지 않는다")
    void sendMessage_ShouldNotPublishFirstMessageEvent_WhenChatroomAlreadyHasMessages() {
        Long chatroomId = 1L;
        Long sendUserId = 10L;
        Long sendMembershipId = 100L;
        Long receiveMembershipId = 200L;
        Long receiveUserId = 20L;
        String message = "test message";
        String clientMessageId = "test-client-message-id";

        when(chatroomQueryService.findPartnerProfile(chatroomId, sendMembershipId))
                .thenReturn(new PartnerProfile(receiveMembershipId, "partner", false));
        when(chatroomQueryService.isEmptyMessageChatroom(chatroomId)).thenReturn(false);
        when(membershipService.findUserIdByMembershipId(receiveMembershipId))
                .thenReturn(receiveUserId);
        when(chatMessageCommandService.sendMessage(eq(chatroomId), eq(sendMembershipId), eq(message), any(Instant.class), any(String.class)))
                .thenReturn(999L);
        when(groupService.findGroupProfileByMembershipId(sendMembershipId))
                .thenReturn(new GroupResponse.GroupProfile(888L, "test group", "group-cover.url"));

        chatWebSocketFacade.sendMessage(chatroomId, sendUserId, sendMembershipId, message, clientMessageId);

        verify(chatEventPublisher, never()).publishFirstMessageSent(any(BuyerFirstMessageSentEvent.class));
    }
}
