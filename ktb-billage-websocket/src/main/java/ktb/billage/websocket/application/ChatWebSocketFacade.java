package ktb.billage.websocket.application;

import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.domain.chat.service.ChatMessageCommandService;
import ktb.billage.domain.chat.service.ChatMessageQueryService;
import ktb.billage.domain.chat.service.ChatroomCommandService;
import ktb.billage.domain.chat.service.ChatroomQueryService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatWebSocketFacade {
    private final MembershipService membershipService;
    private final ChatroomQueryService chatroomQueryService;
    private final ChatroomCommandService chatroomCommandService;
    private final ChatMessageQueryService chatMessageQueryService;
    private final ChatMessageCommandService chatMessageCommandService;

    public ChatResponse.ChatroomMembershipDto joinChatroom(Long chatroomId, Long userId) {
        chatroomQueryService.validateChatroom(chatroomId);

        List<Long> membershipIds = membershipService.findMembershipIds(userId);
        ChatResponse.ChatroomMembershipDto participation = chatroomQueryService.findParticipation(chatroomId, membershipIds);

        chatroomCommandService.readAllMessageBy(participation.chatroomId(), participation.isSeller());
        return participation;
    }

    public void validateParticipating(Long chatroomId, Long userId) {
        List<Long> membershipIds = membershipService.findMembershipIds(userId);
        chatroomQueryService.validateParticipating(chatroomId, membershipIds);
    }

    public ChatSendAckResponse sendMessage(Long chatroomId, Long userId, Long membershipId, String message) {
        membershipService.validateMembershipOwner(userId, membershipId);
        chatroomQueryService.validateParticipating(chatroomId, membershipId);

        Instant now = Instant.now();
        Long messageId = chatMessageCommandService.sendMessage(chatroomId, membershipId, message, now);

        return new ChatSendAckResponse(chatroomId, membershipId, String.valueOf(messageId), message, now);
    }

    public void readMessage(Long chatroomId, Long userId, Long membershipId, String readMessageId) {
        membershipService.validateMembershipOwner(userId, membershipId);
        chatroomQueryService.validateParticipating(chatroomId, membershipId);

        chatMessageQueryService.validateExistingMessage(chatroomId, readMessageId);

        Instant now = Instant.now();
        chatroomCommandService.readMessage(chatroomId, membershipId, readMessageId, now);
    }
}
