package com.app.exception;

import com.app.enums.ErrorCodeEnum;

/**
 * 照片派生处理可重试异常
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class PhotoProcessingRetryableException extends BusinessException {
    public PhotoProcessingRetryableException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    public PhotoProcessingRetryableException(ErrorCodeEnum errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
