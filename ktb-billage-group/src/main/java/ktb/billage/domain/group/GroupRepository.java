package ktb.billage.domain.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    boolean existsByIdAndDeletedAtIsNull(Long groupId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Group> findById(Long groupId);

    Optional<Group> findByIdAndDeletedAtIsNull(Long groupId);
    List<Group> findAllByIdInAndDeletedAtIsNull(List<Long> groupIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Group g
           set g.deletedAt = :deletedAt
         where g.id = :groupId
           and g.deletedAt is null
    """)
    int softDeleteByGroupId(@Param("groupId") Long groupId,
                            @Param("deletedAt") Instant deletedAt);

    @Query("""
        select g
        from Group g
        join Membership m on m.groupId = g.id
        where m.userId = :userId
          and m.deletedAt is null
    """)
    List<Group> findAllByUserId(@Param("userId") Long userId);
}
