package ktb.billage.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findAllByPostId(Long postId);

    Optional<PostImage> findFirstByPostIdAndDeletedAtIsNullOrderBySortOrderAsc(Long postId);

    @Query("""
            select pi
            from PostImage pi
            join fetch pi.post p
            where p.id in :postIds
              and pi.deletedAt is null
              and pi.sortOrder = 1
            """)
    List<PostImage> findAllFirstImagesByPostIds(@Param("postIds") List<Long> postIds);
}
