package ktb.billage.web.common.exception;

import ktb.billage.common.exception.BaseException;

import static ktb.billage.common.exception.ExceptionCode.NOT_SUPPORTED_METHOD;
import static ktb.billage.common.exception.ExceptionCode.PARAMETER_VALIDATION_FAILED;
import static ktb.billage.common.exception.ExceptionCode.SERVER_ERROR;

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

    static ErrorResponse method() {
        return new ErrorResponse(NOT_SUPPORTED_METHOD.getCode());
    }
}
