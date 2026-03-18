package ktb.billage.infra.kafka;

public enum KafkaTopic {
    POST_CREATED("post.created"),
    FIRST_CHAT_SENT("chat.first-sent"),
    USER_BEHAVIOR_CAPTURED("user-behavior.captured"),
    USER_BEHAVIOR_BATCH_REQUESTED("user-behavior.batch-requested"),
    ;


    private final String value;

    KafkaTopic(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
