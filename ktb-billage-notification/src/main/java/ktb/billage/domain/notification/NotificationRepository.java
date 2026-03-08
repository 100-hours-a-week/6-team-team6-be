package ktb.billage.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("""
                    SELECT n FROM Notification n
                    WHERE n.userId = :userId
                    AND n.deletedAt IS NULL
                    ORDER BY n.createdAt DESC, n.id DESC
                    LIMIT 21
            """)
    List<Notification> findTop21ByUserIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(@Param("userId") Long userId);
    
    @Query("""
                    SELECT n FROM Notification n
                    WHERE n.userId = :userId
                    AND n.deletedAt IS NULL
                    AND (n.createdAt < :time OR (n.createdAt = :time AND n.id < :id))
                    ORDER BY n.createdAt DESC, n.id DESC
                    LIMIT 21
            """)
    List<Notification> findNextPageByUserIdAndDeletedAtIsNullWithCursorOrderByCreatedAtDescIdDesc(
            @Param("userId") Long userId,
            @Param("time") Instant time,
            @Param("id") Long id
    );
}
