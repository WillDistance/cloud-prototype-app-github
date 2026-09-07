package com.app.exception;

import com.app.enums.ErrorCodeEnum;
import com.app.pojo.CommonResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResult<Void>> handleAuthenticationException(AuthenticationException exception) {
        log.error("GlobalExceptionHandler handleAuthenticationException msg={}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResult.fail(exception));
    }

    @ExceptionHandler(RequestParameterException.class)
    public ResponseEntity<CommonResult<Void>> handleRequestParameterException(RequestParameterException exception) {
        log.error("GlobalExceptionHandler handleRequestParameterException msg={}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(CommonResult.fail(exception));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResult<Void>> handleBusinessException(BusinessException exception) {
        log.error("GlobalExceptionHandler handleBusinessException msg={}", exception.getMessage(), exception);
        return ResponseEntity.ok(CommonResult.fail(exception));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<CommonResult<Void>> handleValidationException(Exception exception) {
        log.error("GlobalExceptionHandler handleValidationException msg={}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(CommonResult.fail(ErrorCodeEnum.INVALID_REQUEST_PARAMETER));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResult<Void>> handleException(Exception exception) {
        log.error("GlobalExceptionHandler handleException msg={}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResult.fail(ErrorCodeEnum.SYSTEM_BUSY));
    }
}
