package ktb.billage.web.common.exception;

import ktb.billage.exception.BaseException;
import ktb.billage.exception.ExceptionCode;

import static ktb.billage.exception.ExceptionCode.PARAMETER_VALIDATION_FAILED;
import static ktb.billage.exception.ExceptionCode.SERVER_ERROR;

public record ErrorResponse(String code) {

    static ErrorResponse from(BaseException exception) {
        return new ErrorResponse(exception.getCode());
    }

    static ErrorResponse parameter() {
        return new ErrorResponse(PARAMETER_VALIDATION_FAILED.getCode());
    }

    static ErrorResponse internal() {
        return new ErrorResponse(SERVER_ERROR.getCode());
    }
}
