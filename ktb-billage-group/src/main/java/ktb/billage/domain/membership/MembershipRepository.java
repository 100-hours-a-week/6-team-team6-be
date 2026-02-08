package ktb.billage.domain.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByGroupIdAndUserIdAndDeletedAtIsNull(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndDeletedAtIsNull(Long groupId, Long userId);

    Optional<Membership> findByIdAndDeletedAtIsNull(Long membershipId);

    List<Membership> findAllByUserIdAndDeletedAtIsNull(Long userId);

    long countByGroupIdAndDeletedAtIsNull(Long groupId);

    long countByUserIdAndDeletedAtIsNull(Long userId);
}
