package ktb.billage.domain.post.userbehavior;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {
    long countByMembershipIdAndStatus(Long membershipId, UserBehaviorLogStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l
            from UserBehaviorLog l
            where l.membershipId = :membershipId
              and l.status = :status
            order by l.id asc
            """)
    List<UserBehaviorLog> findForUpdateByMembershipIdAndStatusOrderByIdAsc(@Param("membershipId") Long membershipId,
                                                                           @Param("status") UserBehaviorLogStatus status,
                                                                           PageRequest pageRequest);

    List<UserBehaviorLog> findAllByBatchKeyOrderByIdAsc(String batchKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from UserBehaviorLog l
            where l.batchKey = :batchKey
            """)
    void deleteAllByBatchKey(@Param("batchKey") String batchKey);
}
