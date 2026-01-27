package ktb.billage.domain.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.Projections.constructor;
import static ktb.billage.domain.post.QPostImage.postImage;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<PostResponse.ImageInfo> findImageInfos(Long postId) {
        return queryFactory
                .select(constructor(PostResponse.ImageInfo.class,
                        postImage.id,
                        postImage.imageUrl
                ))
                .from(postImage)
                .where(postImage.post.id.eq(postId)
                        .and(postImage.deletedAt.isNull()))
                .orderBy(postImage.sortOrder.asc())
                .fetch();
    }
}
