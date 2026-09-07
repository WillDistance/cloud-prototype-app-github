package com.app.service;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.UserLanguageEnum;
import com.app.exception.BusinessException;
import com.app.mapper.EmailVerificationCodeMapper;
import com.app.mapper.UserMapper;
import com.app.pojo.dto.AuthDto;
import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.AuthRegisterVo;
import com.app.security.JwtTokenService;
import com.app.service.impl.AuthServiceImpl;
import com.app.support.verification.RegisterCodeCache;
import com.app.support.verification.VerificationCodeSender;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class AuthServiceImplTest {
    @Test
    void shouldNormalizeEmailPersistOnlyHashAndRegisterAfterAtomicConsumption() {
        UserMapper userMapper = mock(UserMapper.class);
        EmailVerificationCodeMapper codeMapper = mock(EmailVerificationCodeMapper.class);
        RegisterCodeCache cache = mock(RegisterCodeCache.class);
        VerificationCodeSender sender = mock(VerificationCodeSender.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AuthServiceImpl service = new AuthServiceImpl(userMapper, codeMapper, cache, sender, encoder, tokenService());
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(cache.requestCode(eq("user@example.com"), any())).thenReturn(true);
        when(encoder.encode(anyString())).thenReturn("code-hash");
        when(encoder.matches("123456", "code-hash")).thenReturn(true);
        when(cache.verifyCode("user@example.com", "123456", encoder)).thenReturn(RegisterCodeCache.VerifyResult.VERIFIED);
        EmailVerificationCodeEntity verifiedRecord = new EmailVerificationCodeEntity().setId(9L).setExpireTime(java.time.LocalDateTime.now().plusMinutes(5));
        when(codeMapper.selectOne(any())).thenReturn(null, verifiedRecord);
        when(codeMapper.updateStatusIfCurrent(9L, com.app.enums.VerificationStatusEnum.VERIFIED, com.app.enums.VerificationStatusEnum.INVALIDATED)).thenReturn(1);
        when(encoder.encode("password123")).thenReturn("password-hash");
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> { invocation.getArgument(0, UserEntity.class).setId(7L); return 1; });

        assertTrue(service.sendRegisterCode(new AuthDto.SendRegisterCode().setEmail(" User@Example.COM "), "127.0.0.1"));
        assertTrue(service.verifyRegisterCode(new AuthDto.VerifyRegisterCode().setEmail("USER@example.com").setCode("123456")));
        AuthRegisterVo result = service.register(new AuthDto.Register().setEmail("USER@example.com").setPassword("password123").setConfirmPassword("password123"), "America/New_York", "de-DE,de;q=0.8");

        assertEquals(7L, result.getUserId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("America/New_York", result.getTimeZone());
        assertEquals("de", result.getPreferredLanguage());
        verify(sender).sendRegisterCode(eq("user@example.com"), anyString(), eq(UserLanguageEnum.EN));
        verify(userMapper).insert(any(UserEntity.class));
    }

    @Test
    void shouldRejectInvalidTimeZoneAndExistingEmail() {
        UserMapper userMapper = mock(UserMapper.class);
        AuthServiceImpl service = new AuthServiceImpl(userMapper, mock(EmailVerificationCodeMapper.class), mock(RegisterCodeCache.class), mock(VerificationCodeSender.class), mock(PasswordEncoder.class), tokenService());
        when(userMapper.selectCount(any())).thenReturn(1L);
        BusinessException existing = assertThrows(BusinessException.class, () -> service.sendRegisterCode(new AuthDto.SendRegisterCode().setEmail("user@example.com"), "ip"));
        assertEquals(ErrorCodeEnum.AUTH_EMAIL_ALREADY_EXISTS, existing.getErrorCode());
        BusinessException zone = assertThrows(BusinessException.class, () -> service.register(new AuthDto.Register().setEmail("new@example.com").setPassword("password123").setConfirmPassword("password123"), "UTC+8", "en"));
        assertEquals(ErrorCodeEnum.AUTH_TIME_ZONE_INVALID, zone.getErrorCode());
    }

    @Test
    void shouldRejectRepeatedConsumption() {
        UserMapper userMapper = mock(UserMapper.class);
        RegisterCodeCache cache = mock(RegisterCodeCache.class);
        EmailVerificationCodeMapper codeMapper = mock(EmailVerificationCodeMapper.class);
        AuthServiceImpl service = new AuthServiceImpl(userMapper, codeMapper, cache, mock(VerificationCodeSender.class), mock(PasswordEncoder.class), tokenService());
        when(codeMapper.selectOne(any())).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(new AuthDto.Register().setEmail("user@example.com").setPassword("password123").setConfirmPassword("password123"), "Europe/Berlin", "en"));
        assertEquals(ErrorCodeEnum.AUTH_CODE_NOT_VERIFIED, exception.getErrorCode());
    }

    private JwtTokenService tokenService() {
        return new JwtTokenService("test-only-secret-with-at-least-thirty-two-bytes", java.time.Duration.ofHours(2));
    }
}
