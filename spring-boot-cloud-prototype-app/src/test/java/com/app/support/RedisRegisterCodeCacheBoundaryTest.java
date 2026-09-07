package com.app.support;


import com.app.support.verification.RegisterCodeCache;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Redis注册验证码边界测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class RedisRegisterCodeCacheBoundaryTest {
    @Test
    void shouldDefineFiveMinuteTtlSendFrequencyAndAttemptLimit() {
        assertEquals(Duration.ofMinutes(5), RegisterCodeCache.CODE_TTL);
        assertEquals(Duration.ofSeconds(60), RegisterCodeCache.SEND_INTERVAL);
        assertEquals(5, RegisterCodeCache.MAX_ATTEMPTS);
    }
}
