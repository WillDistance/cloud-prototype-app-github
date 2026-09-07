package com.app.mapper;

import com.app.enums.VerificationStatusEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 认证Mapper事务契约测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class AuthMapperContractTest {
    @Test
    void shouldExposeConditionalVerificationStatusUpdate() throws Exception {
        Method method = EmailVerificationCodeMapper.class.getMethod("updateStatusIfCurrent", Long.class, VerificationStatusEnum.class, VerificationStatusEnum.class);
        assertEquals(int.class, method.getReturnType());
    }
}
