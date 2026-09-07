package com.app.exception;

import com.app.enums.ErrorCodeEnum;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCodeEnum errorCode;

    public BusinessException(ErrorCodeEnum errorCode) {
        this(errorCode, errorCode.getErrorMag());
    }

    public BusinessException(ErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
