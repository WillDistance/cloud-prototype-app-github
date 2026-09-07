package com.app.exception;

import com.app.enums.ErrorCodeEnum;

/**
 * 请求参数异常
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class RequestParameterException extends BusinessException {

    public RequestParameterException(String message) {
        super(ErrorCodeEnum.INVALID_REQUEST_PARAMETER, message);
    }

    public RequestParameterException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }
}
