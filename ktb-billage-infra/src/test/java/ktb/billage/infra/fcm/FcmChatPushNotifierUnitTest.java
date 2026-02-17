package ktb.billage.infra.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import ktb.billage.domain.membership.dto.MembershipProfile;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.user.service.UserPushTokenService;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmChatPushNotifierUnitTest {
    @Mock
    private UserPushTokenService userPushTokenService;
    @Mock
    private MembershipService membershipService;
    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FcmChatPushNotifier fcmChatPushNotifier;

    @Test
    @DisplayName("일치하는 사용자 FCM 토큰이 없다면 웹 푸시 NOP")
    void sendPush_skipsWhenNoTokens() {
        when(userPushTokenService.findTokensByUserId(10L)).thenReturn(List.of());

        fcmChatPushNotifier.sendPush(10L, ack());

        verify(userPushTokenService).findTokensByUserId(10L);
        verifyNoInteractions(membershipService, firebaseMessaging);
    }

    @Test
    @DisplayName("FCM에 메시지를 전송하는 일반 시나리오 경우")
    void sendPush_sendsMappedNotificationAndData() throws Exception {
        when(userPushTokenService.findTokensByUserId(10L)).thenReturn(List.of("t1", "t2"));
        when(membershipService.findMembershipProfile(31L))
                .thenReturn(new MembershipProfile(31L, "sender-nick"));

        BatchResponse response = mock(BatchResponse.class);
        SendResponse sendResponse = mock(SendResponse.class);
        when(sendResponse.isSuccessful()).thenReturn(true);
        when(response.getResponses()).thenReturn(List.of(sendResponse, sendResponse));
        when(response.getSuccessCount()).thenReturn(2);
        when(response.getFailureCount()).thenReturn(0);
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(response);

        fcmChatPushNotifier.sendPush(10L, ack());

        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        verify(firebaseMessaging).sendEachForMulticast(captor.capture());
        MulticastMessage actual = captor.getValue();

        assertEquals(List.of("t1", "t2"), readField(actual, "tokens", List.class));

        Object notification = readField(actual, "notification", Object.class);
        assertEquals("sender-nick", readField(notification, "title", String.class));
        assertEquals("hello", readField(notification, "body", String.class));

        Map<String, String> data = readField(actual, "data", Map.class);
        assertEquals("21", data.get("chatroomId"));
        assertEquals("m-1", data.get("messageId"));
        assertEquals("31", data.get("membershipId"));
    }

    @Test
    @DisplayName("삭제되어야하는 토큰만 삭제시키는 경우")
    void sendPush_deletesOnlyPermanentFailureTokens() throws Exception {
        List<String> tokens = List.of("ok-token", "bad-token-1", "bad-token-2", "retry-token");
        when(userPushTokenService.findTokensByUserId(10L)).thenReturn(tokens);
        when(membershipService.findMembershipProfile(31L))
                .thenReturn(new MembershipProfile(31L, "sender-nick"));

        SendResponse ok = mock(SendResponse.class);
        when(ok.isSuccessful()).thenReturn(true);

        SendResponse unregistered = failedResponse(MessagingErrorCode.UNREGISTERED);
        SendResponse invalidArgument = failedResponse(MessagingErrorCode.INVALID_ARGUMENT);
        SendResponse unavailable = failedResponse(MessagingErrorCode.UNAVAILABLE);

        BatchResponse response = mock(BatchResponse.class);
        when(response.getResponses()).thenReturn(List.of(ok, unregistered, invalidArgument, unavailable));
        when(response.getSuccessCount()).thenReturn(1);
        when(response.getFailureCount()).thenReturn(3);
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(response);

        fcmChatPushNotifier.sendPush(10L, ack());

        verify(userPushTokenService).deleteInvalidTokens(List.of("bad-token-1", "bad-token-2"));
    }

    @Test
    void sendPush_doesNotDeleteOnTransientFailureOnly() throws Exception {
        when(userPushTokenService.findTokensByUserId(10L)).thenReturn(List.of("retry-token"));
        when(membershipService.findMembershipProfile(31L))
                .thenReturn(new MembershipProfile(31L, "sender-nick"));

        SendResponse unavailable = failedResponse(MessagingErrorCode.UNAVAILABLE);
        BatchResponse response = mock(BatchResponse.class);
        when(response.getResponses()).thenReturn(List.of(unavailable));
        when(response.getSuccessCount()).thenReturn(0);
        when(response.getFailureCount()).thenReturn(1);
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(response);

        fcmChatPushNotifier.sendPush(10L, ack());

        verify(userPushTokenService, never()).deleteInvalidTokens(any());
    }

    @Test
    void sendPush_doesNotThrowWhenFirebaseMessagingThrows() throws Exception {
        when(userPushTokenService.findTokensByUserId(10L)).thenReturn(List.of("t1"));
        when(membershipService.findMembershipProfile(31L))
                .thenReturn(new MembershipProfile(31L, "sender-nick"));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .thenThrow(mock(FirebaseMessagingException.class));

        assertDoesNotThrow(() -> fcmChatPushNotifier.sendPush(10L, ack()));
    }

    private ChatSendAckResponse ack() {
        return new ChatSendAckResponse(
                21L,
                31L,
                "m-1",
                "hello",
                Instant.parse("2026-02-17T12:34:56Z")
        );
    }

    private SendResponse failedResponse(MessagingErrorCode errorCode) {
        SendResponse response = mock(SendResponse.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(response.isSuccessful()).thenReturn(false);
        when(response.getException()).thenReturn(exception);
        when(exception.getMessagingErrorCode()).thenReturn(errorCode);
        return response;
    }

    private <T> T readField(Object target, String fieldName, Class<T> fieldType) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(target));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to read field: " + fieldName, e);
        }
    }
}
