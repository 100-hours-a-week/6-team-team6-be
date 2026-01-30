package ktb.billage.contract.image;

public interface ImageStorage {

    String store(byte[] bytes, String contentType, long size);

    String resolveUrl(String imageKey);

    void remove(String imageUrl);
}
