package ktb.billage.application.chat;

import ktb.billage.domain.chat.service.ChatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatQueryService chatQueryService;

    public long countChatroomsByPostId(Long postId) {
        return chatQueryService.countChatroomsByPostId(postId);
    }

    public Long findChatroomIdByPostIdAndBuyerId(Long postId, Long buyerId) {
        return chatQueryService.findChatroomIdByPostIdAndBuyerId(postId, buyerId);
    }
}
