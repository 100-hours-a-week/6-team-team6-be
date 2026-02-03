package ktb.billage.infra.ai;

import io.netty.handler.timeout.TimeoutException;
import ktb.billage.common.exception.AiTimeoutException;
import ktb.billage.domain.post.ai.AiPostDraftClient;
import ktb.billage.domain.post.dto.PostRequest;
import ktb.billage.domain.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.client.MultipartBodyBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.SERVER_ERROR;
import static ktb.billage.common.exception.ExceptionCode.TIME_OUT;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPostDraftWebClient implements AiPostDraftClient {
    private final WebClient webClient;

    @Value("${ai.post-draft-path:/ai/generate}")
    private String postDraftPath;

    @Override
    public PostResponse.PostDraft requestPostDraft(List<PostRequest.ImageComponent> images) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        int limit = Math.min(images.size(), 3);
        for (int i = 0; i < limit; i++) {
            MultipartFile image = toMultipartFile(images.get(i), i + 1);

            String filename = StringUtils.hasText(image.getOriginalFilename())
                    ? image.getOriginalFilename()
                    : "image";

            MediaType contentType = StringUtils.hasText(image.getContentType())
                    ? MediaType.parseMediaType(image.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;

            builder.part("images", image.getResource())
                    .filename(filename)
                    .contentType(contentType);
        }

        try {
            return webClient.post()
                    .uri(postDraftPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(PostResponse.PostDraft.class)
                    .block();
        } catch (TimeoutException timeoutException) {
            log.error("[AI Server Exception] Time out");
            throw new AiTimeoutException(TIME_OUT);
        } catch (Exception e) {
            log.error("[AI Server Exception] Not Handle Error");
            throw new AiTimeoutException(SERVER_ERROR);
        }
    }

    private MultipartFile toMultipartFile(PostRequest.ImageComponent image, int index) {
        String contentType = StringUtils.hasText(image.contentType())
                ? image.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String filename = "image-" + index;
        return new ByteArrayMultipartFile("images", filename, contentType, image.bytes());
    }

    private record ByteArrayMultipartFile(String name, String originalFilename, String contentType,
                                          byte[] bytes) implements MultipartFile {

        @Override
            public String getName() {
                return name;
            }

            @Override
            public String getOriginalFilename() {
                return originalFilename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return bytes == null || bytes.length == 0;
            }

            @Override
            public long getSize() {
                return bytes == null ? 0 : bytes.length;
            }

            @Override
            public byte[] getBytes() {
                return bytes;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(bytes == null ? new byte[0] : bytes);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                throw new IOException("transferTo is not supported");
            }
        }
}
