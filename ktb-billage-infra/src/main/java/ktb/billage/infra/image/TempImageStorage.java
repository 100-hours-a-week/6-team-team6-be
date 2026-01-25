package ktb.billage.infra.image;

import ktb.billage.contract.image.ImageStorage;
import org.springframework.stereotype.Component;

@Component
public class TempImageStorage implements ImageStorage {

    @Override
    public String store(byte[] bytes, String contentType, long size) {
        return "image.store.url";
    }

    @Override
    public void remove(String imageUrl) {

    }

    @Override
    public String getImageUrl(String imageId) {
        return "image.url";
    }
}
