package ktb.billage.infra.image;

import ktb.billage.contract.image.ImageStorage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Profile({"local", "test"})
@Component
public class LocalImageStorage implements ImageStorage {
    private final Map<String, LocalImage> imageDatabase = new ConcurrentHashMap<>();

    @Override
    public String store(byte[] bytes, String contentType, long size) {
        String id = UUID.randomUUID().toString();

        imageDatabase.put(id, new LocalImage(bytes, contentType, size));
        return id;
    }

    @Override
    public String resolveUrl(String imageKey) {
        return imageKey;
    }

    @Override
    public void remove(String imageUrl) {

    }

    private record LocalImage(byte[] bytes, String contentType, long size) {
    }
}
