package ktb.billage.domain.keywordsubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface KeywordSubscriptionRepository extends JpaRepository<KeywordSubscription, Long> {
    
    @Query("""
            SELECT COUNT(ks)
            FROM KeywordSubscription ks
            WHERE ks.userId = :userId
            AND ks.groupId = :groupId
            AND ks.deletedAt IS NULL
            """)
    int countByUserIdAndGroupIdAndDeletedAtIsNull(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Query("""
            SELECT CASE WHEN COUNT(ks) > 0 THEN true ELSE false END
            FROM KeywordSubscription ks
            WHERE ks.userId = :userId
            AND ks.groupId = :groupId
            AND ks.keyword = :keyword
            AND ks.deletedAt IS NULL
            """)
    Boolean existsByUserIdAndGroupIdAndKeywordAndDeletedAtIsNull(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId,
            @Param("keyword") String keyword
    );

    @Query("""
        SELECT ks
        FROM KeywordSubscription ks
        WHERE ks.userId = :userId
        AND ks.groupId = :groupId
        AND ks.deletedAt IS NULL
        ORDER BY ks.createdAt desc, ks.id desc
    """)
    List<KeywordSubscription> findByUserIdAndGroupIdAndDeletedAtIsNull(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE KeywordSubscription ks
        SET ks.deletedAt = :deletedAt
        WHERE ks.groupId = :groupId
        AND ks.userId = :userId
        AND ks.deletedAt IS NULL
    """)
    void softDeleteAllByGroupIdAndUserId(@Param("groupId") Long groupId,
                                         @Param("userId") Long userId,
                                         @Param("deletedAt") Instant deletedAt);

    @Query("""
        SELECT ks
        FROM KeywordSubscription ks
        WHERE ks.groupId = :groupId
        AND ks.userId != :userId
        AND ks.deletedAt IS NULL
    """)
    List<KeywordSubscription> findAllByGroupIdAndNotUserIdAndDeletedAtIsNull(@Param("groupId") Long groupId,
                                                                          @Param("userId") Long userId);
}
