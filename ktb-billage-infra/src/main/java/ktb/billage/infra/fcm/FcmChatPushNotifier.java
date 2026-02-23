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
import ktb.billage.infra.fcm.dto.FcmDataPayload;
import ktb.billage.websocket.application.port.ChatPushNotifier;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnBean(FirebaseMessaging.class)
@RequiredArgsConstructor
public class FcmChatPushNotifier implements ChatPushNotifier {
    private static final String DATA_TYPE_MESSAGE = "CHAT_MESSAGE";

    private final UserPushTokenService userPushTokenService;
    private final MembershipService membershipService;

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendPush(Long receiveUserId, ChatSendAckResponse ack) {
        List<String> tokens = userPushTokenService.findTokensByUserId(receiveUserId);
        if (tokens.isEmpty()) {
            return;
        }

        log.info("FCM push requested. receiveUserId={}, chatroomId={}, messageId={}",
                receiveUserId, ack.chatroomId(), ack.messageId());

        MembershipProfile sender = membershipService.findMembershipProfile(ack.membershipId());

        FcmDataPayload dataPayload = new FcmDataPayload(
                ack.chatroomId(),
                ack.messageId(),
                ack.membershipId()
        );

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putData("type", DATA_TYPE_MESSAGE)
                .putData("title", sender.nickname())
                .putData("body", ack.messageContent())
                .putData("chatroomId", String.valueOf(dataPayload.chatroomId()))
                .putData("messageId", dataPayload.messageId())
                .putData("membershipId", String.valueOf(dataPayload.membershipId()))
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            cleanupInvalidTokens(tokens, response);
            log.info("FCM push sent. receiveUserId={}, tokenCount={}, successCount={}, failureCount={}, createdAt={}",
                    receiveUserId,
                    tokens.size(),
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    ack.createdAt());
        } catch (FirebaseMessagingException e) {
            log.error("FCM push failed. receiveUserId={}, chatroomId={}, messageId={}",
                    receiveUserId, ack.chatroomId(), ack.messageId(), e);
        }
    }

    private void cleanupInvalidTokens(List<String> tokens, BatchResponse response) {
        List<String> invalidTokens = extractInvalidTokens(tokens, response.getResponses());
        if (invalidTokens.isEmpty()) {
            return;
        }

        userPushTokenService.deleteInvalidTokens(invalidTokens);
        log.info("FCM invalid tokens removed. count={}", invalidTokens.size());
    }

    private List<String> extractInvalidTokens(List<String> tokens, List<SendResponse> responses) {
        List<String> invalidTokens = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful() || sendResponse.getException() == null) {
                continue;
            }

            MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                invalidTokens.add(tokens.get(i));
            }
        }
        return invalidTokens;
    }
}
