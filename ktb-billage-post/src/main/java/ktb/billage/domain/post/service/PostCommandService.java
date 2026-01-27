package ktb.billage.domain.post.service;

import ktb.billage.common.exception.PostException;
import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.post.PostImage;
import ktb.billage.domain.post.PostImageRepository;
import ktb.billage.domain.post.PostRepository;
import ktb.billage.domain.post.RentalStatus;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static ktb.billage.common.exception.ExceptionCode.POST_IS_NOT_OWNED_BY_USER;
import static ktb.billage.common.exception.ExceptionCode.POST_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    public PostResponse.Id create(Long membershipId, String title,
                                  String content, List<String> imageUrls, BigDecimal rentalFee, FeeUnit feeUnit) {

        Post post = postRepository.save(new Post(
                membershipId, title, content, rentalFee,
                feeUnit, imageUrls.size()
        ));

        postImageRepository.saveAll(toPostImages(post, imageUrls));

        return new PostResponse.Id(post.getId());
    }

    public PostResponse.Id update(Long postId, Long membershipId, String title,
                                  String content, PostRequest.ImageInfos imageInfos, BigDecimal rentalFee, FeeUnit feeUnit) {

        Post post = findPost(postId);
        validatePostSeller(post, membershipId);

        updateImages(post, imageInfos);
        post.update(title, content, imageInfos.imageInfos().size(), rentalFee, feeUnit);

        return new PostResponse.Id(post.getId());
    }

    public PostResponse.ChangedStatus changeRentalStatus(Long postId, Long membershipId, RentalStatus rentalStatus) {

        Post post = findPost(postId);
        validatePostSeller(post, membershipId);

        post.markAsStatus(rentalStatus);

        return new PostResponse.ChangedStatus(post.getId(), post.getRentalStatus());
    }

    public void delete(Long postId, Long membershipId) {

        Post post = findPost(postId);
        validatePostSeller(post, membershipId);

        post.delete(Instant.now());
    }

    public void validatePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostException(POST_NOT_FOUND);
        }
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

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostException(POST_NOT_FOUND));
    }

    private void validatePostSeller(Post post, Long membershipId) {
        if (!post.isOwner(membershipId)) {
            throw new PostException(POST_IS_NOT_OWNED_BY_USER);
        }
    }

    private void updateImages(Post post, PostRequest.ImageInfos imageInfos) {
        List<PostImage> existingImages = loadExistingImages(post);
        Map<Long, PostImage> existingById = mapImagesById(existingImages);
        Set<Long> requestedIds = toSetForRequestedImageIds(imageInfos);

        softDeleteRemovedImages(existingImages, requestedIds);
        applySortOrdersToExisting(imageInfos, existingById);
        saveNewImages(collectNewImagesWithSortOrder(post, imageInfos));
    }

    private List<PostImage> loadExistingImages(Post post) {
        return postImageRepository.findAllByPostId(post.getId());
    }

    private Map<Long, PostImage> mapImagesById(List<PostImage> images) {
        return images.stream()
                .collect(Collectors.toMap(PostImage::getId, image -> image, (left, right) -> left, HashMap::new));
    }

    private Set<Long> toSetForRequestedImageIds(PostRequest.ImageInfos imageInfos) {
        return imageInfos.imageInfos().stream()
                .map(PostRequest.ImageInfo::postImageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void softDeleteRemovedImages(List<PostImage> existingImages, Set<Long> requestedIds) {
        List<PostImage> toDelete = existingImages.stream()
                .filter(image -> !requestedIds.contains(image.getId()))
                .toList();

        for (PostImage image : toDelete) {
            image.delete(Instant.now());
        }
    }

    private void applySortOrdersToExisting(PostRequest.ImageInfos imageInfos, Map<Long, PostImage> existingById) {
        List<PostRequest.ImageInfo> infos = imageInfos.imageInfos();
        for (int i = 0; i < infos.size(); i++) {
            PostRequest.ImageInfo info = infos.get(i);
            if (info.postImageId() == null) {
                continue;
            }

            PostImage image = existingById.get(info.postImageId());
            if (image != null) {
                image.updateSortOrder(i + 1);
            }
        }
    }

    private List<PostImage> collectNewImagesWithSortOrder(Post post, PostRequest.ImageInfos imageInfos) {
        List<PostImage> toCreate = new ArrayList<>();
        List<PostRequest.ImageInfo> infos = imageInfos.imageInfos();
        for (int i = 0; i < infos.size(); i++) {
            PostRequest.ImageInfo info = infos.get(i);
            if (info.postImageId() != null) {
                continue;
            }

            toCreate.add(new PostImage(post, info.imageUrl(), i + 1));
        }

        return toCreate;
    }

    private void saveNewImages(List<PostImage> newImages) {
        if (!newImages.isEmpty()) {
            postImageRepository.saveAll(newImages);
        }
    }
}
