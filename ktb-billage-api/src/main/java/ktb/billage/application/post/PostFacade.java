package ktb.billage.application.post;

import ktb.billage.domain.post.RentalStatus;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import ktb.billage.domain.post.service.PostCommandService;
import ktb.billage.domain.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostFacade {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    public PostResponse.Id create(Long membershipId, PostRequest.Create request) {
        return postCommandService.create(
                membershipId,
                request.title(),
                request.content(),
                request.imageUrls(),
                request.rentalFee(),
                request.feeUnit()
        );
    }

    public PostResponse.Id update(Long postId, Long membershipId, PostRequest.Update request) {
        return postCommandService.update(
                postId,
                membershipId,
                request.title(),
                request.content(),
                request.imageUrls(),
                request.rentalFee(),
                request.feeUnit()
        );
    }

    public PostResponse.ChangedStatus changeRentalStatus(Long postId, Long membershipId, RentalStatus rentalStatus) {
        return postCommandService.changeRentalStatus(postId, membershipId, rentalStatus);
    }

    public void delete(Long postId, Long membershipId) {
        postCommandService.delete(postId, membershipId);
    }

    public Long getSellerId(Long postId) {
        return postQueryService.findSellerIdByPostId(postId);
    }
}
