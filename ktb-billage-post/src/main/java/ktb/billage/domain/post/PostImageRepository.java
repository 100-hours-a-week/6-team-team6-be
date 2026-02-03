package ktb.billage.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findAllByPostId(Long postId);

    Optional<PostImage> findFirstByPostIdAndDeletedAtIsNullOrderBySortOrderAsc(Long postId);
}
