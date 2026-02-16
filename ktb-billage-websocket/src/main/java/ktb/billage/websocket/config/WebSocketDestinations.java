package ktb.billage.websocket.config;

public final class WebSocketDestinations {
    private WebSocketDestinations() {
    }

    public static final String APP_PREFIX = "/app";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String QUEUE_PREFIX = "/queue";
    public static final String USER_PREFIX = "/user";

    public static final String CHATROOM_TOPIC_PREFIX = TOPIC_PREFIX + "/chatrooms/";
    public static final String USER_INBOX_QUEUE = QUEUE_PREFIX + "/chat-inbox";
    public static final String USER_INBOX_SUBSCRIBE = USER_PREFIX + USER_INBOX_QUEUE;
}
