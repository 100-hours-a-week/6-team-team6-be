package ktb.billage.api.image;

import ktb.billage.common.exception.ImageException;
import ktb.billage.common.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static ktb.billage.common.exception.ExceptionCode.INVALID_IMAGE;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new ImageException(INVALID_IMAGE);
        }

        return ResponseEntity.ok().body(imageService.upload(image.getBytes(), image.getContentType(), image.getSize()));
    }
}
