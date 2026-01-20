package ktb.billage.exception;

public enum ExceptionCode {

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
