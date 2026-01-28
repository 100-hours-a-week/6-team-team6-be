package ktb.billage.infra.image;

import ktb.billage.contract.image.ImageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Profile({"dev", "prod"})
@Component
public class S3ImageStorage implements ImageStorage {
    private static final Duration PRESIGNED_URL_TTL = Duration.ofDays(100);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3ImageStorage(@Value("${s3.bucket}") String bucket,
                          @Value("${s3.region}") String region,
                          @Value("${s3.access-key}") String accessKey,
                          @Value("${s3.secret-key}") String secretKey) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        Region awsRegion = Region.of(region);

        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();
        this.s3Presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public String store(byte[] bytes, String contentType, long size) {
        String key = buildObjectKey(contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        return getImageUrl(key);
    }

    @Override
    public void remove(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String key = extractKey(imageUrl);
        if (key == null || key.isBlank()) {
            return;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    private String getImageUrl(String imageId) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(imageId)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_TTL)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String buildObjectKey(String contentType) {
        String extension = contentTypeToExtension(contentType);
        String id = UUID.randomUUID().toString();
        if (extension == null) {
            return "images/" + id;
        }

        return "images/" + id + "." + extension;
    }

    private String contentTypeToExtension(String contentType) {
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        return null;
    }

    private String extractKey(String imageUrl) {
        URI uri = URI.create(imageUrl);
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return null;
        }

        String bucketPrefix = "/" + bucket + "/";
        if (path.startsWith(bucketPrefix)) {
            return path.substring(bucketPrefix.length());
        }

        return path.startsWith("/") ? path.substring(1) : path;
    }
}
