package ktb.billage.common.exception;

public enum ExceptionCode {

    // auth
    AUTHENTICATION_FAILED("AUTH01", 401),
    AUTH_TOKEN_NOT_FOUND("AUTH02", 401),

    // token
    INVALID_TOKEN("TOKEN01", 401),
    INVALID_RTOKEN("TOKEN02", 401),
    EXPIRED_RTOKEN("TOKEN03", 401),
    EXPIRED_TOKEN("TOKEN04", 401),

    // user
    USER_NOT_FOUND("USER01", 404),
    INVALID_LOGIN_ID("USER02", 400),
    INVALID_NICKNAME("USER03", 400),
    DUPLICATE_LOGIN_ID("USER04", 409),

    // group
    GROUP_NOT_FOUND("GROUP01", 404),
    NOT_GROUP_MEMBER("GROUP02", 403),
    ALREADY_GROUP_MEMBER("GROUP03", 409),
    GROUP_CAPACITY_EXCEEDED("GROUP04", 409),
    USER_GROUP_LIMIT_EXCEEDED("GROUP05", 409),
    INVALID_INVITATION("GROUP06", 404),


    // post
    POST_NOT_FOUND("POST01", 404),
    POST_IS_NOT_OWNED_BY_USER("POST02", 403),

    // chat
    SELF_CHAT_DENIED("CHAT01", 403),
    CHATROOM_NOT_FOUND("CHAT02", 404),
    CHATROOM_NOT_PARTICIPATE("CHAT03", 403),
    ALREADY_EXISTING_CHATROOM("CHAT04", 409),
    FROZEN_CHATROOM("CHAT05", 409),
    CHAT_NOT_FOUND("CHAT06", 404),

    // image
    INVALID_IMAGE("IMAGE01", 400),
    UNSUPPORTED_IMAGE_TYPE("IMAGE02", 415),
    IMAGE_SIZE_LIMIT("IMAGE03", 413),
    IMAGE_NOT_FOUND("IMAGE04", 404),
    IMAGE_HANDLING_FAILED("IMAGE05", 500),

    // ws
    WS_AUTH_TOKEN_NOT_FOUND("WS01", 401),
    WS_ALREADY_CONNECTED("WS02", 400),

    // common
    INVALID_CURSOR("CURSOR01", 400),

    // application
    SERVER_ERROR("SERVER01", 500),
    NOT_SUPPORTED_METHOD("SERVER02", 405),
    TIME_OUT("SERVER03", 504),

    PARAMETER_VALIDATION_FAILED("PARAMETER01", 400),
    ;

    private final String code;
    private final int statusCode;

    ExceptionCode(String code, int statusCode) {
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
