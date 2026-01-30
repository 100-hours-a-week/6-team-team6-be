package ktb.billage.domain.chat.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.common.exception.ChatException;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.ChatroomRepository;
import ktb.billage.domain.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ktb.billage.common.exception.ExceptionCode.CHATROOM_NOT_FOUND;

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
                .orElse(null);
    }

    public ChatResponse.ChatroomSummaryCores findChatroomSummariesByPostIdAndCursor(Long postId, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<ChatResponse.ChatroomSummaryCore> cores = loadChatroomCoresByPostId(postId, decoded);

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

    public ChatResponse.ChatroomSummaryCores findChatroomSummariesByMembershipIdsAndCursor(List<Long> membershipIds, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<ChatResponse.ChatroomSummaryCore> cores = loadChatroomCoresByMembershipIds(membershipIds, decoded);

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

    public List<ChatResponse.ChatroomMembershipDto> findChatroomIdsByMembershipIds(List<Long> membershipIds) {
        return chatroomRepository.findAllByParticipatingIds(membershipIds);
    }

    public void validateChatroom(Long chatroomId) {
        chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ChatException(CHATROOM_NOT_FOUND));
    }

    public ChatResponse.PartnerProfile findPartnerProfile(Long chatroomId, Long myMembershipId) {
        return chatroomRepository.findPartnerProfile(chatroomId, myMembershipId);
    }

    public Chatroom findChatroom(Long chatroomId) {
        return chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ChatException(CHATROOM_NOT_FOUND));
    }

    private CursorCodec.Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return cursorCodec.decode(cursor);
    }

    private List<ChatResponse.ChatroomSummaryCore> loadChatroomCoresByPostId(Long postId, CursorCodec.Cursor decoded) {
        PageRequest pageRequest = PageRequest.of(0, 21);
        if (decoded == null) {
            return chatroomRepository.findTop21SummaryCoresByPostId(postId, pageRequest);
        }

        return chatroomRepository.findNextSummaryCorePageByPostId(
                postId,
                decoded.time(),
                decoded.id(),
                pageRequest
        );
    }

    private List<ChatResponse.ChatroomSummaryCore> loadChatroomCoresByMembershipIds(List<Long> membershipIds, CursorCodec.Cursor decoded) {
        PageRequest pageRequest = PageRequest.of(0, 21);
        if (decoded == null) {
            return chatroomRepository.findTop21SummaryCoresByMembershipIds(membershipIds, pageRequest);
        }

        return chatroomRepository.findNextSummaryCorePageByMembershipIds(
                membershipIds,
                decoded.time(),
                decoded.id(),
                pageRequest
        );
    }
}
