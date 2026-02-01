package ktb.billage.domain.chat.service;

import ktb.billage.common.exception.ChatException;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.ChatroomRepository;
import ktb.billage.domain.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

import static ktb.billage.common.exception.ExceptionCode.ALREADY_EXISTING_CHATROOM;
import static ktb.billage.common.exception.ExceptionCode.SELF_CHAT_DENIED;


@Service
@RequiredArgsConstructor
public class ChatroomCommandService {
    private final ChatroomQueryService chatroomQueryService;
    private final ChatroomRepository chatroomRepository;

    public ChatResponse.Id create(Long postId, Long sellerId, Long buyerId) {
        validateNotSelfChat(sellerId, buyerId);

        if (isAlreadyExistingChatroom(postId, buyerId)) {
            throw new ChatException(ALREADY_EXISTING_CHATROOM);
        }

        Chatroom chatroom = chatroomRepository.save(new Chatroom(postId, buyerId));

        return new ChatResponse.Id(chatroom.getId());
    }

    @Transactional
    public void readAllMessageBy(Long chatroomId, boolean isSeller) {
        Chatroom chatroom = chatroomQueryService.findChatroom(chatroomId);

        if (isSeller) {
            chatroom.readAllBySeller(Instant.now());
        } else {
            chatroom.readAllByBuyer(Instant.now());
        }
    }

    public void readMessage(Long chatroomId, Long membershipId, String readMessageId, Instant readAt) {
        Chatroom chatroom = chatroomQueryService.findChatroom(chatroomId);

        chatroom.readBy(membershipId, readMessageId, readAt);
    }

    private void validateNotSelfChat(Long sellerId, Long buyerId) {
        if (Objects.equals(sellerId, buyerId)) {
            throw new ChatException(SELF_CHAT_DENIED);
        }
    }

    private boolean isAlreadyExistingChatroom(Long postId, Long buyerId) {
        return chatroomRepository.existsByPostIdAndBuyerId(postId, buyerId);
    }
}
