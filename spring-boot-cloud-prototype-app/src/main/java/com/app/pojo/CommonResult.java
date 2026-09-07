package com.app.pojo;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 通用接口返回结果
 *
 * @param <T> 业务数据类型
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {
    private ErrorCodeEnum errorCode = ErrorCodeEnum.E00000;

    private String errorMag = ErrorCodeEnum.E00000.getErrorMag();

    private T data;

    public CommonResult(T data) {
        this.data = data;
    }

    public CommonResult(ErrorCodeEnum errorCode, String errorMag) {
        this.errorCode = errorCode;
        this.errorMag = errorMag;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(data);
    }

    public static <T> CommonResult<T> fail(ErrorCodeEnum errorCode) {
        return new CommonResult<>(errorCode, errorCode.getErrorMag());
    }

    public static <T> CommonResult<T> fail(BusinessException exception) {
        return new CommonResult<>(exception.getErrorCode(), exception.getMessage());
    }
}
