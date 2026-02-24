package ktb.billage.domain.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByGroup(Group group);

    Optional<Invitation> findByToken(String token);
}
