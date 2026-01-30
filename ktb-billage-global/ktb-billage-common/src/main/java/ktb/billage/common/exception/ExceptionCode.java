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

    // post
    POST_NOT_FOUND("POST01", 404),
    POST_IS_NOT_OWNED_BY_USER("POST02", 403),

    // chat
    SELF_CHAT_DENIED("CHAT01", 403),
    CHATROOM_NOT_FOUND("CHAT02", 404),

    // image
    INVALID_IMAGE("IMAGE01", 400),
    UNSUPPORTED_IMAGE_TYPE("IMAGE02", 415),
    IMAGE_SIZE_LIMIT("IMAGE03", 413),
    IMAGE_NOT_FOUND("IMAGE04", 404),

    // common
    INVALID_CURSOR("CURSOR01", 400),

    // application
    SERVER_ERROR("SERVER01", 500),
    NOT_SUPPORTED_METHOD("SERVER02", 405),

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
