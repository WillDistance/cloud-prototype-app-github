package com.app.security;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JWT令牌服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class JwtTokenServiceTest {
    private static final String SECRET = "test-only-secret-with-at-least-thirty-two-bytes";

    @Test
    void shouldIssueTokenWithUserTimeZoneAndTokenId() {
        JwtTokenService service = new JwtTokenService(SECRET, Duration.ofMinutes(30));
        String token = service.issue(7L, "Asia/Shanghai");

        AuthenticatedUser user = service.parse(token);

        assertEquals(7L, user.userId());
        assertEquals("Asia/Shanghai", user.timeZone());
        assertEquals(36, user.tokenId().length());
    }

    @Test
    void shouldRejectForgedAndExpiredTokensUniformly() throws InterruptedException {
        JwtTokenService service = new JwtTokenService(SECRET, Duration.ofMillis(1));
        String token = service.issue(7L, "UTC");
        Thread.sleep(5);

        AuthenticationException expired = assertThrows(AuthenticationException.class, () -> service.parse(token));
        assertEquals(ErrorCodeEnum.AUTH_TOKEN_EXPIRED, expired.getErrorCode());

        AuthenticationException forged = assertThrows(AuthenticationException.class,
                () -> new JwtTokenService("another-test-secret-with-at-least-thirty-two-bytes", Duration.ofMinutes(1)).parse(token));
        assertEquals(ErrorCodeEnum.AUTH_REQUIRED, forged.getErrorCode());
    }
}
