package ktb.billage.domain.post;

import ktb.billage.domain.PostJpaTestApplication;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.group.GroupRepository;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.membership.MembershipRepository;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PostJpaTestApplication.class)
class PostJpaTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Test
    @DisplayName("findTop21ByMyPosts: 내 게시글 21개를 최신순으로 조회하고 groupId 포함")
    void findTop21ByMyPosts() {
        // given
        User me = userRepository.save(new User("tester", "Tester1!"));
        Group group1 = groupRepository.save(new Group("group-1", "dummy.cover"));
        Membership membership1 = membershipRepository.save(new Membership(group1.getId(), me.getId(), "member"));

        Group group2 = groupRepository.save(new Group("group-2", "dummy.cover"));
        Membership membership2 = membershipRepository.save(new Membership(group2.getId(), me.getId(), "member"));

        User notMe = userRepository.save(new User("not-me", "NotMe!"));
        Membership notMeMembership1 = membershipRepository.save(new Membership(group1.getId(), notMe.getId(), "member"));

        Group group3 = groupRepository.save(new Group("group-3", "dummy.cover"));
        Membership notMeMembership3 = membershipRepository.save(new Membership(group3.getId(), notMe.getId(), "member"));


        List<Long> myPostIds = new ArrayList<>();
        List<Long> otherPostIds = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            Post post1 = postRepository.save(new Post(
                    membership1.getId(),
                    "title-" + i,
                    "content-" + i,
                    BigDecimal.valueOf(1000),
                    FeeUnit.HOUR,
                    1
            ));
            postImageRepository.save(new PostImage(post1, "img-" + i, 1));
            myPostIds.add(post1.getId());

            Post post2 = postRepository.save(new Post(
                    membership2.getId(),
                    "title-" + i,
                    "content-" + i,
                    BigDecimal.valueOf(1000),
                    FeeUnit.HOUR,
                    1
            ));
            postImageRepository.save(new PostImage(post2, "img-" + i, 1));
            myPostIds.add(post2.getId());

            Post post3 = postRepository.save(new Post(
                    notMeMembership1.getId(),
                    "title-" + i,
                    "content-" + i,
                    BigDecimal.valueOf(1000),
                    FeeUnit.HOUR,
                    1
            ));
            postImageRepository.save(new PostImage(post3, "img-" + i, 1));
            otherPostIds.add(post3.getId());

            Post post4 = postRepository.save(new Post(
                    notMeMembership3.getId(),
                    "title-" + i,
                    "content-" + i,
                    BigDecimal.valueOf(1000),
                    FeeUnit.HOUR,
                    1
            ));
            postImageRepository.save(new PostImage(post4, "img-" + i, 1));
            otherPostIds.add(post4.getId());
        }

        // when
        List<PostResponse.MySummary> result = postRepository
                .findTop21ByMyPosts(
                        List.of(membership1.getId(), membership2.getId()),
                        org.springframework.data.domain.PageRequest.of(0, 21)
                );

        // then
        assertThat(result).hasSize(21);
        assertThat(result.stream().map(PostResponse.MySummary::groupId).toList()).doesNotContain(group3.getId());
        List<Long> resultIds = result.stream().map(PostResponse.MySummary::postId).toList();
        assertThat(resultIds).allMatch(myPostIds::contains);
        assertThat(resultIds).noneMatch(otherPostIds::contains);

        List<Long> ids = result.stream().map(PostResponse.MySummary::postId).toList();
        List<Long> sorted = ids.stream().sorted((a, b) -> Long.compare(b, a)).toList();
        assertThat(ids).isEqualTo(sorted);
    }
}
