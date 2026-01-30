package ktb.billage.web.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import ktb.billage.common.exception.BaseException;
import ktb.billage.common.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception) {
        log.warn("[Base exception] code : {}", exception.getCode());

        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode());

        return ResponseEntity.status(status)
                .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(HttpServletRequest request) {
        log.warn("[Unsupported HTTP method] on : {}, requested method : {}", request.getRequestURI(), request.getMethod());

        return ErrorResponse.method();
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRequestValidationFailException(HttpServletRequest request) {
        log.warn("[Parameter validation fail] on : {}", request.getRequestURI());

        return ErrorResponse.parameter();
    }

    @ExceptionHandler({
            Exception.class,
            InternalException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception exception) {
        log.error("[Unexpected Exception] exception : {}", exception.getClass().getName(), exception);

        return ErrorResponse.internal();
    }
}
