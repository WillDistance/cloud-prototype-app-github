package com.app.utils;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.BusinessException;
import jakarta.annotation.Nullable;
import org.springframework.util.Assert;

/**
 * 异常断言
 *
 * @author yanlei
 * @since 2022-09-24
 */
public class AssertUtil extends Assert {
    /**
     * 为null则抛出异常
     *
     * @param object  对象
     * @param eduCode 异常码
     */
    public static void nonNull(@Nullable Object object, ErrorCodeEnum eduCode) {
        if (object == null) {
            throw new BusinessException(eduCode);
        }
    }

    /**
     * 为true通过，为false则抛出异常
     *
     * @param expression 条件
     * @param eduCode    异常码
     */
    public static void isTrue(boolean expression, ErrorCodeEnum eduCode) {
        if (!expression) {
            throw new BusinessException(eduCode);
        }
    }
}