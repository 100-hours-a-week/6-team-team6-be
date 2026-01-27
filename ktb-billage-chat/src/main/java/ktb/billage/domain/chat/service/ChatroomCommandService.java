package ktb.billage.domain.chat.service;

import ktb.billage.common.exception.ChatException;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.ChatroomRepository;
import ktb.billage.domain.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static ktb.billage.common.exception.ExceptionCode.SELF_CHAT_DENIED;


@Service
@RequiredArgsConstructor
public class ChatroomCommandService {
    private final ChatroomRepository chatroomRepository;

    public ChatResponse.Messages create(Long postId, Long sellerId, Long buyerId) {
        validateNotSelfChat(sellerId, buyerId);

        Chatroom chatroom = chatroomRepository.save(new Chatroom(postId, buyerId));

        return new ChatResponse.Messages(chatroom.getId(), List.of(), null, false);
    }

    private void validateNotSelfChat(Long sellerId, Long buyerId) {
        if (Objects.equals(sellerId, buyerId)) {
            throw new ChatException(SELF_CHAT_DENIED);
        }
    }
}
