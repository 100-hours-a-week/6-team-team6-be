package ktb.billage.domain.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop21ByOrderByCreatedAtDescIdDesc();

    @Query("""
            select p from Post p
            where p.createdAt < :cursorTime
               or (p.createdAt = :cursorTime and p.id < :cursorId)
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findNextPage(@Param("cursorTime") Instant cursorTime,
                            @Param("cursorId") Long cursorId,
                            Pageable pageable);
}
