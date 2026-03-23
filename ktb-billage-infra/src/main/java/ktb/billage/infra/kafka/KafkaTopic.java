package ktb.billage.infra.kafka;

public enum KafkaTopic {
    POST_CREATED("post.created"),
    FIRST_CHAT_SENT("chat.first-sent"),
    ;


    private final String value;

    KafkaTopic(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
