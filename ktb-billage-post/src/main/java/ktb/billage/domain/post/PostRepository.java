package ktb.billage.domain.post;

import ktb.billage.domain.post.dto.PostResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndDeletedAtIsNull(Long postId);

    @Query("""
            select p from Post p
            join Membership m on m.id = p.sellerId
            where m.groupId = :groupId
              and p.deletedAt is null
              order by p.updatedAt desc, p.id desc
            """)
    List<Post> findTop21ByGroupIdOrderByUpdatedAtDescIdDesc(@Param("groupId") Long groupId, Pageable pageable);

    @Query(value = """
            select p.*
            from post p
            join membership m on m.id = p.membership_id
            where m.group_id = :groupId
              and p.deleted_at is null
              and match(p.title) against (:keyword in boolean mode)
            order by p.updated_at desc, p.id desc
            """, nativeQuery = true)
    List<Post> findTop21ByGroupIdAndContainingKeywordOrderByUpdatedAtDescIdDesc(@Param("groupId") Long groupId,
                                                                                @Param("keyword") String keyword,
                                                                                Pageable pageable);

    @Query("""
            select p from Post p
            join Membership m on m.id = p.sellerId
            where m.groupId = :groupId
               and p.deletedAt is null
               and (p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId))
            order by p.updatedAt desc, p.id desc
            """)
    List<Post> findNextPage(@Param("groupId") Long groupId,
                            @Param("cursorTime") Instant cursorTime,
                            @Param("cursorId") Long cursorId,
                            Pageable pageable);

    @Query(value = """
            select p.*
            from post p
            join membership m on m.id = p.membership_id
            where m.group_id = :groupId
              and p.deleted_at is null
              and match(p.title) against (:keyword in boolean mode)
              and (p.updated_at < :cursorTime
               or (p.updated_at = :cursorTime and p.id < :cursorId))
            order by p.updated_at desc, p.id desc
            """, nativeQuery = true)
    List<Post> findNextPageByKeyword(@Param("groupId") Long groupId,
                                     @Param("keyword") String keyword,
                                     @Param("cursorTime") Instant cursorTime,
                                     @Param("cursorId") Long cursorId,
                                     Pageable pageable);

    @Query("""
        select m.groupId
        from Post p
        join Membership m on p.sellerId = m.id
        where p.id = :postId
            and p.deletedAt is null
    """)
    Long findGroupIdByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Post p
           set p.deletedAt = :deletedAt
         where p.sellerId = :membershipId
           and p.deletedAt is null
    """)
    int softDeleteBySellerId(@Param("membershipId") Long membershipId,
                             @Param("deletedAt") Instant deletedAt);

    @Query("""
        select new ktb.billage.domain.post.dto.PostResponse$MySummary(
            p.id,
            p.title,
            pi.id,
            pi.imageUrl,
            p.updatedAt,
            m.groupId
        )
        from Post p
        join Membership m on p.sellerId = m.id
        join PostImage pi on pi.post = p and pi.deletedAt is null and pi.sortOrder = 1
        where p.deletedAt is null
          and p.sellerId IN :membershipIds
        order by p.updatedAt desc, p.id desc
    """)
    List<PostResponse.MySummary> findTop21ByMyPosts(@Param("membershipIds") List<Long> membershipIds,
                                                    Pageable pageable);

    @Query("""
        select new ktb.billage.domain.post.dto.PostResponse$MySummary(
            p.id,
            p.title,
            pi.id,
            pi.imageUrl,
            p.updatedAt,
            m.groupId
        )
        from Post p
        join Membership m on p.sellerId = m.id
        join PostImage pi on pi.post = p and pi.deletedAt is null and pi.sortOrder = 1
        where p.deletedAt is null
          and p.sellerId IN :membershipIds
          and (p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId))
        order by p.updatedAt desc, p.id desc
    """)
    List<PostResponse.MySummary> findNextMyPosts(@Param("membershipIds") List<Long> membershipIds,
                                                 @Param("cursorTime") Instant cursorTime,
                                                 @Param("cursorId") Long cursorId,
                                                 Pageable pageable);
}
