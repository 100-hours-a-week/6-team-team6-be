package ktb.billage.common.image;

import ktb.billage.common.exception.ImageException;
import ktb.billage.contract.image.ImageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.util.Set;

import static ktb.billage.common.exception.ExceptionCode.IMAGE_SIZE_LIMIT;
import static ktb.billage.common.exception.ExceptionCode.UNSUPPORTED_IMAGE_TYPE;

@Service
public class ImageService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");
    private static final String GROUP_DEFAULT_COVER_RPEFIX = "/group-cover-images";

    private final ImageStorage imageStorage;
    private final DataSize maxImageSize;

    public ImageService(ImageStorage imageStorage,
                        @Value("${spring.servlet.multipart.max-file-size}") DataSize maxImageSize) {
        this.imageStorage = imageStorage;
        this.maxImageSize = maxImageSize;
    }

    public String upload(byte[] imageBytes, String contentType, long size) {
        if (size > maxImageSize.toBytes()) {
            throw new ImageException(IMAGE_SIZE_LIMIT);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ImageException(UNSUPPORTED_IMAGE_TYPE);
        }

        return imageStorage.store(imageBytes, contentType, size);
    }

    public String resolveUrl(String imageKey) {
        if (imageKey.startsWith(GROUP_DEFAULT_COVER_RPEFIX)) {
            return imageKey;
        }

        return imageStorage.resolveUrl(imageKey);
    }

    public void delete(String imageUrl) {
        imageStorage.remove(imageUrl);
    }
}
