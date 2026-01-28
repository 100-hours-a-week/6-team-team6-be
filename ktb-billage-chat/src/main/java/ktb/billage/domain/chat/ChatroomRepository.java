package ktb.billage.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import ktb.billage.domain.chat.dto.ChatResponse;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    long countByPostId(Long postId);

    Optional<Chatroom> findFirstByPostIdAndBuyerId(Long postId, Long buyerId);

    @Query("""
            select new ktb.billage.domain.chat.dto.ChatResponse$ChatroomSummaryCore(
                c.id,
                c.buyerId,
                m.id,
                m.createdAt,
                m.content,
                c.sellerLastReadMessageId,
                c.buyerLastReadMessageId
            )
            from Chatroom c
            join ChatMessage m
            where c.postId = :postId
              and c.deletedAt is null
              and c.lastMessageId is not null
            order by m.createdAt desc, c.id desc
            """)
    List<ChatResponse.ChatroomSummaryCore> findTop21SummaryCoresByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("""
            select new ktb.billage.domain.chat.dto.ChatResponse$ChatroomSummaryCore(
                c.id,
                c.buyerId,
                m.id,
                m.createdAt,
                m.content,
                c.sellerLastReadMessageId,
                c.buyerLastReadMessageId
            )
            from Chatroom c
            join ChatMessage m
            where c.postId = :postId
              and c.deletedAt is null
              and c.lastMessageId is not null
              and (m.createdAt < :time or (m.createdAt = :time and c.id < :id))
            order by m.createdAt desc, c.id desc
            """)
    List<ChatResponse.ChatroomSummaryCore> findNextSummaryCorePage(@Param("postId") Long postId,
                                                                   @Param("time") Instant time,
                                                                   @Param("id") Long id,
                                                                   Pageable pageable);
}
