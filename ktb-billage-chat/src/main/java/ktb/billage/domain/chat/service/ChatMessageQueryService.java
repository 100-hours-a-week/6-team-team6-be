package ktb.billage.domain.chat.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.domain.chat.ChatMessage;
import ktb.billage.domain.chat.ChatMessageRepository;
import ktb.billage.domain.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryService {
    private final ChatMessageRepository chatMessageRepository;

    private final CursorCodec cursorCodec;

    public ChatResponse.Messages getMessagesByCursor(Long chatroomId, Long buyerId, String cursor) {
        CursorCodec.Cursor decoded = decodeCursor(cursor);
        List<ChatMessage> messages = loadMessages(chatroomId, decoded);

        boolean hasNext = messages.size() > 20;
        List<ChatMessage> pageMessages = hasNext ? messages.subList(0, 20) : messages;

        String nextCursor = null;
        if (hasNext) {
            ChatMessage last = pageMessages.getLast();
            nextCursor = cursorCodec.encode(last.getCreatedAt(), last.getId());
        }

        List<ChatResponse.MessageItem> messageItems = pageMessages.stream()
                .map(message -> toMessageItem(message, buyerId))
                .toList();

        return new ChatResponse.Messages(chatroomId, messageItems, new ChatResponse.CursorDto(nextCursor, hasNext));
    }

    public List<Long> countUnreadPartnerMessagesByAndBetween(List<ChatResponse.ChatroomSummaryCore> chatroomSummaries, Long myId, boolean isSeller) {
        List<Long> unreadMessageCounts = new ArrayList<>();
        for (ChatResponse.ChatroomSummaryCore chatroomSummary : chatroomSummaries) {
            Long unreadCount;
            if (chatroomSummary.sellerLastReadMessageId() == null) {
                unreadCount = chatMessageRepository.countPartnerAllMessages(chatroomSummary.chatroomId(), myId);

            } else {
                unreadCount = chatMessageRepository.countPartnerMessagesBetween(chatroomSummary.chatroomId(), myId, chatroomSummary.lastMessageId(),
                        isSeller ? chatroomSummary.sellerLastReadMessageId() : chatroomSummary.buyerLastReadMessageId());
            }
            unreadMessageCounts.add(unreadCount);
        }

        return unreadMessageCounts;
    }

    private CursorCodec.Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return cursorCodec.decode(cursor);
    }

    private List<ChatMessage> loadMessages(Long chatroomId, CursorCodec.Cursor decoded) {
        if (decoded == null) {
            return chatMessageRepository.findTop21ByChatroomIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(chatroomId);
        }

        return chatMessageRepository.findNextPage(
                chatroomId,
                decoded.time(),
                decoded.id(),
                PageRequest.of(0, 21)
        );
    }

    private ChatResponse.MessageItem toMessageItem(ChatMessage message, Long buyerId) {
        String who = message.sentBy(buyerId) ? "me" : "partner";

        return new ChatResponse.MessageItem(
                message.getId(),
                who,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
