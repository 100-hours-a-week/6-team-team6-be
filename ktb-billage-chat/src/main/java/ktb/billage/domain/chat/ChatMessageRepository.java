package ktb.billage.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop21ByChatroomIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(Long chatroomId);

    @Query("""
        select m from ChatMessage m
        where m.chatroomId = :chatroomId
          and m.deletedAt is null
          and (m.createdAt < :time or (m.createdAt = :time and m.id < :id))
        order by m.createdAt desc, m.id desc
        """)
    List<ChatMessage> findNextPage(@Param("chatroomId") Long chatroomId,
                                   @Param("time") Instant time,
                                   @Param("id") Long id,
                                   Pageable pageable);
}
