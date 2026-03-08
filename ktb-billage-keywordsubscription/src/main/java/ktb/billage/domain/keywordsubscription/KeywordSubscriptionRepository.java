package ktb.billage.domain.keywordsubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
