package ktb.billage.domain.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByGroupIdAndUserId(Long groupId, Long userId);

}
