package ktb.billage.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop21ByChatroomIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(Long chatroomId);

    @Query("""
        select m from ChatMessage m
        where m.chatroom.id = :chatroomId
          and m.deletedAt is null
          and (m.createdAt < :time or (m.createdAt = :time and m.id < :id))
        order by m.createdAt desc, m.id desc
        """)
    List<ChatMessage> findNextPage(@Param("chatroomId") Long chatroomId,
                                   @Param("time") Instant time,
                                   @Param("id") Long id,
                                   Pageable pageable);

    @Query("""
        select count(m) from ChatMessage m
        where m.chatroom.id = :chatroomId
          and m.deletedAt is null
          and m.senderId != :senderId
    """)
    Long countPartnerAllMessages(@Param("chatroomId") Long chatroomId, @Param("senderId") Long senderId);

    @Query("""
        select count(m) from ChatMessage m
        where m.chatroom.id = :chatroomId
          and m.deletedAt is null
          and m.senderId != :senderId
          and m.id > :lastReadMessageId
          and m.id <= :lastMessageId
    """)
    Long countPartnerMessagesBetween(@Param("chatroomId") Long chatroomId,
                                     @Param("senderId") Long senderId,
                                     @Param("lastMessageId") Long lastMessageId,
                                     @Param("lastReadMessageId") Long lastReadMessageId);

    @Query("""
        select count(m)
        from ChatMessage m
        join m.chatroom r
        where m.deletedAt is null
            and r.buyerId not in :memberIds
    """)
    Long countAllMessagesNotIncludeSenderIds(@Param("chatroomId") Long chatroomId, @Param("memberIds") Set<Long> memberIds);

    @Query("""
        select count(m)
        from ChatMessage m
        where m.chatroom.id = :chatroomId
          and m.deletedAt is null
          and m.senderId = :partnerId
    """)
    Long countAllPartnerMessagesCount(@Param("chatroomId") Long chatroomId, @Param("partnerId") Long partnerId);

    @Query("""
        select count(m)
        from ChatMessage m
        where m.chatroom.id = :chatroomId
          and m.deletedAt is null
          and m.senderId = :partnerId
          and m.id > :lastReadMessageId
          and m.id <= :lastMessageId
    """)
    Long countUnreadPartnerMessagesBetween(@Param("chatroomId") Long chatroomId,
                                           @Param("partnerId") Long partnerId,
                                           @Param("lastMessageId") Long lastMessageId,
                                           @Param("lastReadMessageId") Long lastReadMessageId);

    @Query("""
        select count(msg.id)
        from ChatMessage msg
        join msg.chatroom room
        where room.id = :chatroomId
            and room.deletedAt is null
            and msg.deletedAt is null
            and msg.senderId != :membershipId
            and room.lastMessageId >= msg.id
            and (
                   (:isSeller = true and coalesce(room.sellerLastReadMessageId, 0) < msg.id)
                or (:isSeller = false and coalesce(room.buyerLastReadMessageId, 0) < msg.id)
            )
    """)
    Long findUnreadMessageCountByChatroomAndMembership(@Param("chatroomId") Long chatroomId,
                                                       @Param("membershipId") Long membershipId,
                                                       @Param("isSeller") boolean isSeller);
}
