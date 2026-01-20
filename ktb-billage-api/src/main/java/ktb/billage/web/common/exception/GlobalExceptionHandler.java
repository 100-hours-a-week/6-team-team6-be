package ktb.billage.web.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import ktb.billage.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception) {
        log.warn("[Base exception] code : {}", exception.getCode());

        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode());

        return ResponseEntity.status(status)
                .body(ErrorResponse.from(exception));
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception exception) {
        log.error("[Unexpected Exception] exception : {}", exception.getClass().getName());

        return ErrorResponse.internal();
    }
}
