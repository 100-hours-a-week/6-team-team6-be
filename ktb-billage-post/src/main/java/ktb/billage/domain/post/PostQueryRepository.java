package ktb.billage.domain.post;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.Projections.constructor;
import static ktb.billage.domain.chatroom.QChatroom.chatroom;
import static ktb.billage.domain.post.QPost.post;
import static ktb.billage.domain.post.QPostImage.postImage;
import static ktb.billage.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {
    private final JPAQueryFactory queryFactory;

    public PostResponse.Detail findPostDetail(Long postId, Long userId, Long membershipId) {
        Expression<Boolean> isSellerExpr = post.sellerId.eq(membershipId);

        Expression<Long> activeChatroomCountExpr = new CaseBuilder()
                .when(post.sellerId.eq(membershipId))
                .then(JPAExpressions.select(chatroom.count())
                        .from(chatroom)
                        .where(chatroom.postId.eq(post.id)))
                .otherwise(-1L);

        Expression<Long> chatroomIdExpr = new CaseBuilder()
                .when(post.sellerId.eq(membershipId))
                .then(-1L)
                .otherwise(Expressions.numberTemplate(Long.class,
                        "coalesce({0}, {1})",
                        JPAExpressions.select(chatroom.id)
                                .from(chatroom)
                                .where(chatroom.postId.eq(post.id)
                                        .and(chatroom.buyerId.eq(membershipId)))
                                .limit(1),
                        -1L));

        Tuple row = queryFactory
                .select(
                        post.title,
                        post.content,
                        post.sellerId,
                        user.nickname,
                        user.avatarUrl,
                        post.rentalFee,
                        post.feeUnit,
                        post.rentalStatus,
                        post.updatedAt,
                        isSellerExpr,
                        activeChatroomCountExpr,
                        chatroomIdExpr
                )
                .from(post)
                .join(user).on(user.id.eq(userId))
                .where(post.id.eq(postId))
                .fetchOne();

        if (row == null) {
            return null;
        }

        List<PostResponse.ImageInfo> imageInfos = queryFactory
                .select(constructor(PostResponse.ImageInfo.class,
                        postImage.id,
                        postImage.imageUrl
                ))
                .from(postImage)
                .where(postImage.post.id.eq(postId)
                        .and(postImage.deletedAt.isNull()))
                .orderBy(postImage.sortOrder.asc())
                .fetch();

        return new PostResponse.Detail(
                row.get(post.title),
                row.get(post.content),
                new PostResponse.ImageUrls(imageInfos),
                row.get(post.sellerId),
                row.get(user.nickname),
                row.get(user.avatarUrl),
                row.get(post.rentalFee),
                row.get(post.feeUnit),
                row.get(post.rentalStatus),
                row.get(post.updatedAt),
                row.get(isSellerExpr),
                row.get(chatroomIdExpr),
                row.get(activeChatroomCountExpr)
        );
    }
}
