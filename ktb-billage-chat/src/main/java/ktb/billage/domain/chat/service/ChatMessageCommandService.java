package ktb.billage.domain.chat.service;

import ktb.billage.common.exception.ChatException;
import ktb.billage.domain.chat.ChatMessage;
import ktb.billage.domain.chat.ChatMessageRepository;
import ktb.billage.domain.chat.Chatroom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static ktb.billage.common.exception.ExceptionCode.FROZEN_CHATROOM;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandService {
    private final ChatroomQueryService chatroomQueryService;

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public Long sendMessage(Long chatroomId, Long membershipId, String message, Instant sendAt) {
        Chatroom chatroom = chatroomQueryService.findChatroom(chatroomId);
        if (!chatroom.isActive()) {
            throw new ChatException(FROZEN_CHATROOM);
        }

        ChatMessage chatMessage = chatMessageRepository.save(new ChatMessage(membershipId, chatroom, message, sendAt));

        chatroom.sendMessage(chatMessage.getId(), membershipId, sendAt);

        return chatMessage.getId();
    }
}
