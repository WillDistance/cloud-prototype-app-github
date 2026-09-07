package com.app.exception;

import com.app.enums.ErrorCodeEnum;

/**
 * OSS回调拒绝异常
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class OssCallbackRejectedException extends BusinessException {
    public OssCallbackRejectedException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    public OssCallbackRejectedException(ErrorCodeEnum errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
