package com.app.service;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.UserStatusEnum;
import com.app.exception.BusinessException;
import com.app.mapper.EmailVerificationCodeMapper;
import com.app.mapper.UserMapper;
import com.app.pojo.dto.AuthDto;
import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.AuthLoginVo;
import com.app.security.JwtTokenService;
import com.app.service.impl.AuthServiceImpl;
import com.app.support.verification.RegisterCodeCache;
import com.app.support.verification.VerificationCodeSender;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 阶段三认证服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class AuthStageThreeServiceTest {
    private final UserMapper userMapper = mock(UserMapper.class);
    private final EmailVerificationCodeMapper codeMapper = mock(EmailVerificationCodeMapper.class);
    private final RegisterCodeCache cache = mock(RegisterCodeCache.class);
    private final VerificationCodeSender sender = mock(VerificationCodeSender.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtTokenService jwt = new JwtTokenService("test-only-secret-with-at-least-thirty-two-bytes", java.time.Duration.ofHours(2));
    private final AuthServiceImpl service = new AuthServiceImpl(userMapper, codeMapper, cache, sender, encoder, jwt);

    @Test
    void shouldLoginAndUseStoredTimeZone() {
        UserEntity user = new UserEntity().setId(8L).setEmail("user@example.com").setPasswordHash("hash")
                .setTimeZone("Europe/Berlin").setStatus(UserStatusEnum.ACTIVE);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(encoder.matches("password123", "hash")).thenReturn(true);

        AuthLoginVo result = service.login(new AuthDto.Login().setEmail("USER@example.com").setPassword("password123"));

        assertEquals(8L, jwt.parse(result.getAccessToken()).userId());
        assertEquals("Europe/Berlin", jwt.parse(result.getAccessToken()).timeZone());
        verify(userMapper).updateById(user);
    }

    @Test
    void shouldRejectWrongPasswordAndDisabledAccount() {
        UserEntity user = new UserEntity().setPasswordHash("hash").setStatus(UserStatusEnum.ACTIVE);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(encoder.matches("wrong", "hash")).thenReturn(false);
        assertEquals(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS,
                assertThrows(BusinessException.class, () -> service.login(new AuthDto.Login().setEmail("user@example.com").setPassword("wrong"))).getErrorCode());

        user.setStatus(UserStatusEnum.DISABLED);
        when(encoder.matches("password123", "hash")).thenReturn(true);
        assertEquals(ErrorCodeEnum.AUTH_ACCESS_DENIED,
                assertThrows(BusinessException.class, () -> service.login(new AuthDto.Login().setEmail("user@example.com").setPassword("password123"))).getErrorCode());
    }

    @Test
    void shouldConsumeVerifiedResetCodeAndUpdatePassword() {
        UserEntity user = new UserEntity().setId(8L).setEmail("user@example.com").setStatus(UserStatusEnum.ACTIVE);
        EmailVerificationCodeEntity record = new EmailVerificationCodeEntity().setId(11L).setUserId(8L)
                .setExpireTime(LocalDateTime.now().plusMinutes(5));
        when(userMapper.selectOne(any())).thenReturn(user);
        when(codeMapper.selectOne(any())).thenReturn(record);
        when(codeMapper.updateStatusIfCurrent(any(), any(), any())).thenReturn(1);
        when(cache.consumeVerified("reset:user@example.com")).thenReturn(true);
        when(encoder.encode("newPassword123")).thenReturn("new-hash");

        assertTrue(service.resetPassword(new AuthDto.ResetPassword().setEmail("user@example.com")
                .setNewPassword("newPassword123").setConfirmPassword("newPassword123")));
        assertEquals("new-hash", user.getPasswordHash());
        verify(userMapper).updateById(user);
    }
}
