package ktb.billage.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import ktb.billage.common.image.ImageService;
import ktb.billage.domain.membership.dto.MembershipProfile;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.user.service.UserPushTokenService;
import ktb.billage.websocket.application.port.ChatPushNotifier;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class FcmChatPushNotifier extends FcmPush<ChatSendAckResponse> implements ChatPushNotifier {
    private static final String CHAT_TYPE_MESSAGE = "CHAT_MESSAGE";
    private static final String CHATROOM_PATH_PREFIX = "/chat";

    private final MembershipService membershipService;
    private final ImageService imageService;

    public FcmChatPushNotifier(UserPushTokenService userPushTokenService,
                               MembershipService membershipService,
                               FirebaseMessaging firebaseMessaging,
                               ImageService imageService) {
        super(userPushTokenService, firebaseMessaging);
        this.membershipService = membershipService;
        this.imageService = imageService;
    }

    @Override
    public void sendPush(Long receiveUserId, ChatSendAckResponse ack) {
        log.info("[FCM CHAT PUSH][START] receiveUserId={}, chatroomId={}, messageId={}, membershipId={}",
                receiveUserId, ack.chatroomId(), ack.messageId(), ack.membershipId());
        send(receiveUserId, ack);
    }

    @Override
    protected Map<String, String> buildData(ChatSendAckResponse ack) {
        MembershipProfile sender = membershipService.findMembershipProfile(ack.membershipId());

        Map<String, String> data = new LinkedHashMap<>();
        data.put(KEY_TYPE, CHAT_TYPE_MESSAGE);
        data.put(KEY_CREATED_AT, String.valueOf(ack.createdAt()));
        data.put(KEY_TITLE, APP_NAME);
        data.put(KEY_BODY, ack.messageContent());
        data.put(KEY_SUBTITLE, sender.nickname());
        data.put(KEY_IMAGE_URL, imageService.resolveDefaultAvatarUrl());
        data.put(KEY_TARGET_URL, buildTargetUrl(ack));
        return data;
    }

    @Override
    protected String logContext(ChatSendAckResponse ack) {
        return "chatroomId=" + ack.chatroomId() + ", messageId=" + ack.messageId() + ", membershipId=" + ack.membershipId();
    }

    private String buildTargetUrl(ChatSendAckResponse ack) {
        return String.format("%s/%s", CHATROOM_PATH_PREFIX, ack.chatroomId());
    }
}
