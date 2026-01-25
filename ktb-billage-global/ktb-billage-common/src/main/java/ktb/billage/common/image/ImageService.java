package ktb.billage.common.image;

import ktb.billage.common.exception.ImageException;
import ktb.billage.contract.image.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

import static ktb.billage.common.exception.ExceptionCode.IMAGE_SIZE_LIMIT;
import static ktb.billage.common.exception.ExceptionCode.UNSUPPORTED_IMAGE_TYPE;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");

    private final ImageStorage imageStorage;

    public String upload(byte[] imageBytes, String contentType, long size) {
        if (size > MAX_IMAGE_SIZE_BYTES) {
            throw new ImageException(IMAGE_SIZE_LIMIT);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ImageException(UNSUPPORTED_IMAGE_TYPE);
        }

        return imageStorage.store(imageBytes, contentType, size);
    }

    public void delete(String imageUrl) {
        imageStorage.remove(imageUrl);
    }
}
