package ktb.billage.domain.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop21ByOrderByUpdatedAtDescIdDesc();
    List<Post> findTop21ByTitleContainingOrderByUpdatedAtDescIdDesc(String keyword);

    @Query("""
            select p from Post p
            where p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId)
            order by p.updatedAt desc, p.id desc
            """)
    List<Post> findNextPage(@Param("cursorTime") Instant cursorTime,
                            @Param("cursorId") Long cursorId,
                            Pageable pageable);

    @Query("""
            select p from Post p
            where p.title like concat('%', :keyword, '%')
              and (p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId))
            order by p.updatedAt desc, p.id desc
            """)
    List<Post> findNextPageByKeyword(@Param("keyword") String keyword,
                                     @Param("cursorTime") Instant cursorTime,
                                     @Param("cursorId") Long cursorId,
                                     Pageable pageable);
}
