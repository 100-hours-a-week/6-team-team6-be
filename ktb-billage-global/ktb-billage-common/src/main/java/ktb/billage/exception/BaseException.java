package ktb.billage.exception;

public class BaseException extends RuntimeException {
    private final ExceptionCode exceptionCode;

    public BaseException(ExceptionCode exceptionCode) {
        super(exceptionCode.getCode());
        this.exceptionCode = exceptionCode;
    }

    public int getStatusCode() {
        return this.exceptionCode.getStatusCode();
    }

    public String getCode() {
        return this.exceptionCode.getCode();
    }
}
