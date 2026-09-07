package com.app.exception;

import com.app.enums.ErrorCodeEnum;

/**
 * 鉴权异常
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCodeEnum errorCode, String message) {
        super(errorCode, message);
    }
}
