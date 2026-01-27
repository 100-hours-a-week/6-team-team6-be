package ktb.billage.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    long countByPostId(Long postId);

    Optional<Chatroom> findFirstByPostIdAndBuyerId(Long postId, Long buyerId);
}
