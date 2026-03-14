package ktb.billage.infra.kafka.post;

public enum PostKafkaTopic {
    CREATED("post.created");

    private final String value;

    PostKafkaTopic(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
