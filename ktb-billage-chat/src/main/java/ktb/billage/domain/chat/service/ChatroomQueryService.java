package ktb.billage.domain.chat.service;

import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.ChatroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatroomQueryService {
    private final ChatroomRepository chatroomRepository;

    public long countChatroomsByPostId(Long postId) {
        return chatroomRepository.countByPostId(postId);
    }

    public Long findChatroomIdByPostIdAndBuyerId(Long postId, Long buyerId) {
        return chatroomRepository.findFirstByPostIdAndBuyerId(postId, buyerId)
                .map(Chatroom::getId)
                .orElse(-1L);
    }
}
