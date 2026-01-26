package ktb.billage.domain.post.service;

import ktb.billage.application.port.in.GroupPolicyFacade;
import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.post.PostImage;
import ktb.billage.domain.post.PostImageRepository;
import ktb.billage.domain.post.PostRepository;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    private final GroupPolicyFacade groupPolicyFacade;

    @Transactional
    public PostResponse.Id create(Long groupId, Long userId, String title,
                                  String content, List<String> imageUrls, BigDecimal rentalFee, FeeUnit feeUnit) {

        Long membershipId = groupPolicyFacade.requireMembershipIdForAccess(groupId, userId);

        Post post = postRepository.save(new Post(
                membershipId, title, content, rentalFee,
                feeUnit, imageUrls.size()
        ));

        postImageRepository.saveAll(toPostImages(post, imageUrls));

        return new PostResponse.Id(post.getId());
    }

    private List<PostImage> toPostImages(Post post, List<String> imageUrls) {
        return IntStream.range(0, imageUrls.size())
                .mapToObj(index -> new PostImage(
                        post,
                        imageUrls.get(index),
                        index + 1
                ))
                .toList();
    }
}
