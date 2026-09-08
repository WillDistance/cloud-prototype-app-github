package com.app.exception;

import com.app.enums.ErrorCodeEnum;

/**
 * 对象存储异常
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class OssException extends BusinessException {
    public OssException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    public OssException(ErrorCodeEnum errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
