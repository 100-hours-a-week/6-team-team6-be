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
            join ChatMessage m on m.chatroom.id = c.id
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
            join ChatMessage m on m.chatroom.id = c.id
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

    @Query("""
            select new ktb.billage.domain.chat.dto.ChatResponse$ChatroomMembershipDto(
                c.id,
                case when c.buyerId in :membershipIds then c.buyerId else p.sellerId end,
                case when c.buyerId in :membershipIds then false else true end
            )
            from Chatroom c
            join Post p on c.postId = p.id
            where c.deletedAt is null
                and c.lastMessageId is not null
                and (c.buyerId in :membershipIds
                    or p.sellerId in :membershipIds)
    """)
    List<ChatResponse.ChatroomMembershipDto> findAllByParticipantIds(@Param("membershipIds") List<Long> membershipIds);

    @Query("""
                select new ktb.billage.domain.chat.dto.ChatResponse$PartnerProfile(
                    m.id
                   )
            from Chatroom c
            join Post p on c.postId = p.id
            join Membership m on m.id = (case when c.buyerId = :myId then p.sellerId else c.buyerId end)
            where c.id = :chatroomId
                and c.deletedAt is null
            """)
    ChatResponse.PartnerProfile findPartnerProfile(@Param("chatroomId") Long chatroomId, @Param("myId") Long myId);
}
