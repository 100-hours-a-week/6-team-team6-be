package ktb.billage.infra.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import ktb.billage.domain.user.service.UserPushTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class FcmPush<T> {
    protected static final String APP_NAME = "Billages";
    protected static final String KEY_TYPE = "type";
    protected static final String KEY_CREATED_AT = "createdAt";
    protected static final String KEY_TITLE = "title";
    protected static final String KEY_BODY = "body";
    protected static final String KEY_SUBTITLE = "subtitle";
    protected static final String KEY_IMAGE_URL = "imageUrl";
    protected static final String KEY_TARGET_URL = "targetUrl";

    private final UserPushTokenService userPushTokenService;
    private final FirebaseMessaging firebaseMessaging;

    protected void send(Long receiveUserId, T payload) {
        List<String> tokens = userPushTokenService.findTokensByUserId(receiveUserId);
        if (tokens.isEmpty()) {
            log.info("FCM push skipped. receiveUserId={}, no tokens found", receiveUserId);
            return;
        }

        log.info("FCM push requested. receiveUserId={}, context={}", receiveUserId, logContext(payload));

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(buildData(payload))
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            cleanupInvalidTokens(tokens, response);
            log.info("FCM push sent. receiveUserId={}, fcmTokens={}, tokenCount={}, successCount={}, failureCount={}, context={}",
                    receiveUserId, formatTokenResults(tokens, response.getResponses()), tokens.size(), response.getSuccessCount(), response.getFailureCount(), logContext(payload));
        } catch (FirebaseMessagingException e) {
            log.error("FCM push failed. receiveUserId={}, fcmTokens={}, context={}",
                    receiveUserId, tokens, logContext(payload), e);
        }
    }

    protected abstract Map<String, String> buildData(T payload);

    protected abstract String logContext(T payload);

    private void logPerTokenResult(Long receiveUserId, List<String> tokens, List<String> deviceIds, List<SendResponse> responses) {
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            String token = i < tokens.size() ? tokens.get(i) : "UNKNOWN";
            String deviceId = i < deviceIds.size() ? deviceIds.get(i) : "UNKNOWN";
            if (sendResponse.isSuccessful()) {
                log.info("FCM push token result. receiveUserId={}, index={}, deviceId={}, fcmToken={}, success=true",
                        receiveUserId, i, deviceId, token);
                continue;
            }

            String errorCode = sendResponse.getException() == null
                    ? "UNKNOWN"
                    : String.valueOf(sendResponse.getException().getMessagingErrorCode());
            log.warn("FCM push token result. receiveUserId={}, index={}, deviceId={}, fcmToken={}, success=false, errorCode={}",
                    receiveUserId, i, deviceId, token, errorCode);
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

    private List<String> formatTokenResults(List<String> tokens, List<SendResponse> responses) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            String token = i < tokens.size() ? tokens.get(i) : "UNKNOWN";
            if (sendResponse.isSuccessful()) {
                results.add("token=" + token + ",success=true");
                continue;
            }

            String errorCode = sendResponse.getException() == null
                    ? "UNKNOWN"
                    : String.valueOf(sendResponse.getException().getMessagingErrorCode());
            results.add("token=" + token + ",success=false,errorCode=" + errorCode);
        }
        return results;
    }
}
