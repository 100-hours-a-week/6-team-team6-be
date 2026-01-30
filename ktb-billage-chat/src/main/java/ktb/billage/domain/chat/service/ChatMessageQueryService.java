package ktb.billage.domain.chat.service;

import ktb.billage.common.cursor.CursorCodec;
import ktb.billage.domain.chat.ChatMessage;
import ktb.billage.domain.chat.ChatMessageRepository;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatroomQueryService chatroomQueryService;

    private final CursorCodec cursorCodec;

    public ChatResponse.Messages getMessagesByCursor(Long chatroomId, Long requestorId, String cursor) {
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
                .map(message -> toMessageItem(message, requestorId))
                .toList();

        return new ChatResponse.Messages(chatroomId, messageItems, new ChatResponse.CursorDto(nextCursor, hasNext));
    }

    public List<Long> countUnreadPartnerMessagesByChatroomSummariesForSeller(List<ChatResponse.ChatroomSummaryCore> chatroomSummaries, Long sellerId) {
        List<Long> unreadMessageCounts = new ArrayList<>();
        for (ChatResponse.ChatroomSummaryCore summary : chatroomSummaries) {
            Long unreadCount;

            if (summary.sellerLastReadMessageId() == null) {
                unreadCount = chatMessageRepository.countPartnerAllMessages(summary.chatroomId(), sellerId);
            } else {
                unreadCount = chatMessageRepository.countPartnerMessagesBetween(summary.chatroomId(), sellerId,
                        summary.lastMessageId(), summary.sellerLastReadMessageId());
            }

            unreadMessageCounts.add(unreadCount);
        }
        return unreadMessageCounts;
    }

    public List<Long> countUnreadPartnerMessagesByChatroomSummariesAndMembershipIdForRole(ChatResponse.ChatroomSummaryCores cores, Set<Long> membershipIds) {
        List<Long> unreadMessageCounts = new ArrayList<>();
        for (ChatResponse.ChatroomSummaryCore summary : cores.chatroomSummaryCores()) {
            Long unreadCount;

            Chatroom chatroom = chatroomQueryService.findChatroom(summary.chatroomId());
            boolean isBuyer = chatroom.isBuyerContaining(membershipIds);

            if (isBuyer) {
                if (summary.buyerLastReadMessageId() == null) {
                    unreadCount = chatMessageRepository.countAllMessagesNotIncludeSenderIds(summary.chatroomId(), membershipIds);
                } else {
                    unreadCount = chatMessageRepository.countMessagesNotIncludeSenderIdsBetween(summary.chatroomId(), membershipIds,
                            summary.lastMessageId(), summary.buyerLastReadMessageId());
                }
            } else {
                if (summary.sellerLastReadMessageId() == null) {
                    unreadCount = chatMessageRepository.countAllMessagesIncludeSenderIds(summary.chatroomId(), membershipIds);
                } else {
                    unreadCount = chatMessageRepository.countMessagesIncludeSenderIdsBetween(summary.chatroomId(), membershipIds,
                            summary.lastMessageId(), summary.sellerLastReadMessageId());
                }
            }

            unreadMessageCounts.add(unreadCount);
        }

        return unreadMessageCounts;
    }

    public Long countUnreadMessagesByChatInfo(List<ChatResponse.ChatroomMembershipDto> chatroomMembershipDtos) {
        return chatroomMembershipDtos.stream()
                .mapToLong(dto ->
                        chatMessageRepository.findUnreadMessageCountByChatroomAndMembership(
                                dto.chatroomId(), dto.membershipId(), dto.isSeller()
                        )
                )
                .sum();
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

    private ChatResponse.MessageItem toMessageItem(ChatMessage message, Long requestorId) {
        String who = message.sentBy(requestorId) ? "me" : "partner";

        return new ChatResponse.MessageItem(
                message.getId(),
                who,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
