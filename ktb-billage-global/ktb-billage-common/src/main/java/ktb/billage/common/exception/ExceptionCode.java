package ktb.billage.common.exception;

public enum ExceptionCode {

    // auth
    AUTHENTICATION_FAILED("AUTH01", 401),
    AUTH_TOKEN_NOT_FOUND("AUTH02", 401),

    // token
    INVALID_TOKEN("TOKEN01", 401),
    INVALID_RTOKEN("TOKEN02", 401),
    EXPIRED_RTOKEN("TOKEN03", 401),


    // user
    USER_NOT_FOUND("USER01", 404),
    INVALID_LOGIN_ID("USER02", 400),
    INVALID_NICKNAME("USER03", 400),
    DUPLICATE_LOGIN_ID("USER04", 409),

    // application
    SERVER_ERROR("SERVER01", 500),

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
