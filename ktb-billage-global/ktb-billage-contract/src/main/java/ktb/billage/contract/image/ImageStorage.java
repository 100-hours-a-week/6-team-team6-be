package ktb.billage.contract.image;

public interface ImageStorage {

    String store(byte[] bytes, String contentType, long size);

    void remove(String imageUrl);

    String getImageUrl(String imageId);
}
