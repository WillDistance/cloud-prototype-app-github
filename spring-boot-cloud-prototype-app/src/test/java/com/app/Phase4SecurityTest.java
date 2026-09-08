package com.app;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import com.app.records.AuthenticatedUser;
import com.app.security.JwtTokenService;
import com.app.support.ratelimit.InMemoryRateLimiter;
import com.app.utils.UserContextHolderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段四JWT、用户上下文和限流测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase4SecurityTest {
    @AfterEach
    void clearContext() {
        UserContextHolderUtil.clear();
    }

    @Test
    void JwtUtil签发并解析用户令牌() {
        JwtTokenService service = new JwtTokenService(
                "phase4-test-secret-with-at-least-thirty-two-bytes", Duration.ofMinutes(2));

        String token = service.issue(9L, "Asia/Shanghai");
        AuthenticatedUser user = service.parse(token);

        assertEquals(9L, user.userId());
        assertEquals("Asia/Shanghai", user.timeZone());
        assertTrue(user.tokenId() != null && !user.tokenId().isBlank());
    }

    @Test
    void 签名错误的令牌应返回认证异常() {
        JwtTokenService service = new JwtTokenService(
                "phase4-test-secret-with-at-least-thirty-two-bytes", Duration.ofMinutes(2));

        String token = service.issue(9L, "Asia/Shanghai");
        JwtTokenService anotherService = new JwtTokenService(
                "another-test-secret-with-at-least-thirty-two-bytes", Duration.ofMinutes(2));

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> anotherService.parse(token));

        assertEquals(ErrorCodeEnum.AUTH_REQUIRED, exception.getErrorCode());
    }

    @Test
    void 过期令牌应返回令牌过期异常() throws InterruptedException {
        JwtTokenService service = new JwtTokenService(
                "phase4-expiration-secret-with-at-least-thirty-two-bytes", Duration.ofMillis(1));
        String token = service.issue(9L, "Asia/Shanghai");
        Thread.sleep(30);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> service.parse(token));

        assertEquals(ErrorCodeEnum.AUTH_TOKEN_EXPIRED, exception.getErrorCode());
    }

    @Test
    void 用户上下文支持设置读取和清理() {
        AuthenticatedUser user = new AuthenticatedUser(9L, "Asia/Shanghai", "token-id");

        UserContextHolderUtil.set(user);
        assertEquals(user, UserContextHolderUtil.get());
        assertEquals(9L, UserContextHolderUtil.getUserId());
        assertEquals("Asia/Shanghai", UserContextHolderUtil.getZoneId().getId());

        UserContextHolderUtil.clear();
        assertTrue(UserContextHolderUtil.get() == null);
    }

    @Test
    void 单机限流器达到上限后拒绝请求() {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter();

        assertTrue(limiter.tryAcquire("phase4", 2, Duration.ofMinutes(1)));
        assertTrue(limiter.tryAcquire("phase4", 2, Duration.ofMinutes(1)));
        assertFalse(limiter.tryAcquire("phase4", 2, Duration.ofMinutes(1)));
    }
}
