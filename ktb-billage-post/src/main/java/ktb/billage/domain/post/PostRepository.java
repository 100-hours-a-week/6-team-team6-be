package ktb.billage.domain.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop21ByDeletedAtIsNullOrderByUpdatedAtDescIdDesc();
    List<Post> findTop21ByDeletedAtIsNullAndTitleContainingOrderByUpdatedAtDescIdDesc(String keyword);

    @Query("""
            select p from Post p
            where p.deletedAt is null
               and (p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId))
            order by p.updatedAt desc, p.id desc
            """)
    List<Post> findNextPage(@Param("cursorTime") Instant cursorTime,
                            @Param("cursorId") Long cursorId,
                            Pageable pageable);

    @Query("""
            select p from Post p
            where p.deletedAt is null
              and p.title like concat('%', :keyword, '%')
              and (p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId))
            order by p.updatedAt desc, p.id desc
            """)
    List<Post> findNextPageByKeyword(@Param("keyword") String keyword,
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
        select p
        from Post p
        where p.deletedAt is null
          and p.sellerId IN :membershipIds
          order by p.updatedAt desc, p.id desc
    """)
    List<Post> findTop21ByDeletedAtIsNullAndSellerIdInMembershipIdsOrderByUpdatedAtDescIdDesc(@Param("membershipIds") List<Long> membershipIds,
                                                                                              Pageable pageable);

    @Query("""
        select p
        from Post p
        where p.deletedAt is null
          and p.sellerId IN :membershipIds
          and (p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId))
        order by p.updatedAt desc, p.id desc
    """)
    List<Post> findNextMyPosts(@Param("membershipIds") List<Long> membershipIds,
                               @Param("cursorTime") Instant cursorTime,
                               @Param("cursorId") Long cursorId,
                               Pageable pageable);
}
