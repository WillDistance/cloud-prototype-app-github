package com.app.security;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT请求拦截器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class JwtAuthenticationInterceptorTest {
    private final JwtTokenService tokens = new JwtTokenService("test-only-secret-with-at-least-thirty-two-bytes", Duration.ofHours(2));
    private final JwtAuthenticationInterceptor interceptor = new JwtAuthenticationInterceptor(tokens);

    @Test
    void shouldBuildAndClearRequestUserContextFromVerifiedToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokens.issue(25L, "Asia/Shanghai"));

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals(25L, UserContextHolder.get().userId());
        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);
        assertEquals(null, UserContextHolder.get());
    }

    @Test
    void shouldRejectMissingAndForgedAuthorization() {
        AuthenticationException missing = assertThrows(AuthenticationException.class,
                () -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
        assertEquals(ErrorCodeEnum.AUTH_REQUIRED, missing.getErrorCode());

        MockHttpServletRequest forged = new MockHttpServletRequest();
        forged.addHeader("Authorization", "Bearer forged.token.value");
        AuthenticationException invalid = assertThrows(AuthenticationException.class,
                () -> interceptor.preHandle(forged, new MockHttpServletResponse(), new Object()));
        assertEquals(ErrorCodeEnum.AUTH_REQUIRED, invalid.getErrorCode());
    }
}
