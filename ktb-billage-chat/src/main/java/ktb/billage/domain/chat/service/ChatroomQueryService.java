package ktb.billage.domain.chat.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.ChatroomRepository;
import ktb.billage.domain.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatroomQueryService {
    private final ChatroomRepository chatroomRepository;

    private final CursorCodec cursorCodec;

    public long countChatroomsByPostId(Long postId) {
        return chatroomRepository.countByPostId(postId);
    }

    public Long findChatroomIdByPostIdAndBuyerId(Long postId, Long buyerId) {
        return chatroomRepository.findFirstByPostIdAndBuyerId(postId, buyerId)
                .map(Chatroom::getId)
                .orElse(-1L);
    }

    public ChatResponse.ChatroomSummaryCores getChatroomSummariesByPostIdAndCursor(Long postId, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<ChatResponse.ChatroomSummaryCore> cores = loadChatroomCores(postId, decoded);

        boolean hasNext = cores.size() > 20;
        List<ChatResponse.ChatroomSummaryCore> pageCores = hasNext ? cores.subList(0, 20) : cores;

        String nextCursor;
        if (hasNext) {
            ChatResponse.ChatroomSummaryCore last = pageCores.getLast();
            nextCursor = cursorCodec.encode(last.lastMessageAt(), last.chatroomId());
        } else {
            nextCursor = null;
        }

        return new ChatResponse.ChatroomSummaryCores(
                pageCores,
                new ChatResponse.CursorDto(nextCursor, hasNext)
        );
    }

    private CursorCodec.Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return cursorCodec.decode(cursor);
    }

    private List<ChatResponse.ChatroomSummaryCore> loadChatroomCores(Long postId, CursorCodec.Cursor decoded) {
        PageRequest pageRequest = PageRequest.of(0, 21);
        if (decoded == null) {
            return chatroomRepository.findTop21SummaryCoresByPostId(postId, pageRequest);
        }

        return chatroomRepository.findNextSummaryCorePage(
                postId,
                decoded.time(),
                decoded.id(),
                pageRequest
        );
    }
}
