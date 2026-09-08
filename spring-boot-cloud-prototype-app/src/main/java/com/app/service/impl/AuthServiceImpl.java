package com.app.service.impl;

import com.app.enums.*;
import com.app.exception.AuthenticationException;
import com.app.exception.BusinessException;
import com.app.exception.RequestParameterException;
import com.app.mapper.EmailVerificationCodeMapper;
import com.app.mapper.UserMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.dto.*;
import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.AuthTokenVo;
import com.app.pojo.vo.UserVo;
import com.app.security.JwtTokenService;
import com.app.service.AuthService;
import com.app.support.auth.VerificationCodeSender;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证业务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_INTERVAL = Duration.ofSeconds(60);
    private static final int MAX_FAILURES = 5;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserStorageAccountMapper storageMapper;
    @Autowired
    private EmailVerificationCodeMapper codeMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private VerificationCodeSender sender;

    @Override
    public void sendRegisterCode(AuthEmailCodeRequest request, String ip) {
        send(request, ip, VerificationPurposeEnum.REGISTER.getValue());
    }

    @Override
    public void sendResetPasswordCode(AuthEmailCodeRequest request, String ip) {
        send(request, ip, VerificationPurposeEnum.RESET_PASSWORD.getValue());
    }

    /**
     * 生成验证码并保存验证码状态，再调用发送适配器发送验证码。
     *
     * @param request 业务请求参数
     * @param ip 请求来源IP
     * @param purpose 验证码用途
     */
    private void send(AuthEmailCodeRequest request, String ip, String purpose) {
        String email = normalizeEmail(request.getEmail());
        UserEntity existing = userMapper.selectByEmail(email);
        if (VerificationPurposeEnum.REGISTER.getValue().equals(purpose) && existing != null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_EMAIL_ALREADY_EXISTS);
        }
        if (VerificationPurposeEnum.RESET_PASSWORD.getValue().equals(purpose) && existing == null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS);
        }
        String key = key(email, purpose);
        Boolean available = redis.opsForValue().setIfAbsent(key + ":cooldown", "1", SEND_INTERVAL);
        if (Boolean.FALSE.equals(available)) {
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_SEND_TOO_FREQUENT);
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        EmailVerificationCodeEntity entity = new EmailVerificationCodeEntity().setId(IdWorker.getId()).setEmail(email).setUserId(existing == null ? null : existing.getId()).setPurpose(purpose).setCode(passwordEncoder.encode(code)).setStatus(VerificationStatusEnum.PENDING.getValue()).setSendCount(1).setVerifyFailCount(0).setExpireTime(LocalDateTime.now().plus(CODE_TTL)).setRequestIp(ip);
        codeMapper.insert(entity);
        redis.opsForValue().set(key, code, CODE_TTL);
        sender.send(email, code);
    }

    @Override
    public void verifyRegisterCode(VerifyCodeRequest request) {
        verify(request, VerificationPurposeEnum.REGISTER.getValue());
    }

    @Override
    public void verifyResetPasswordCode(VerifyCodeRequest request) {
        verify(request, VerificationPurposeEnum.RESET_PASSWORD.getValue());
    }

    /**
     * 校验验证码并将验证成功状态写入缓存和数据库。
     *
     * @param request 业务请求参数
     * @param purpose 验证码用途
     */
    private void verify(VerifyCodeRequest request, String purpose) {
        String email = normalizeEmail(request.getEmail());
        EmailVerificationCodeEntity record = codeMapper.selectLatest(email, purpose);
        if (record == null || record.getExpireTime().isBefore(LocalDateTime.now()) || !VerificationStatusEnum.PENDING.getValue().equals(record.getStatus()) || record.getVerifyFailCount() >= MAX_FAILURES) {
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_INVALID);
        }
        String cached = redis.opsForValue().get(key(email, purpose));
        boolean match = cached != null ? cached.equals(request.getCode()) : passwordEncoder.matches(request.getCode(), record.getCode());
        if (!match) {
            codeMapper.incrementFailures(record.getId());
            if (record.getVerifyFailCount() + 1 >= MAX_FAILURES) {
                throw new BusinessException(ErrorCodeEnum.AUTH_CODE_ATTEMPTS_EXCEEDED);
            }
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_INVALID);
        }
        codeMapper.markVerified(record.getId());
        redis.delete(key(email, purpose));
        redis.delete(key(email, purpose) + ":cooldown");
        redis.opsForValue().set(key(email, purpose) + ":verified", "1", CODE_TTL);
    }

    @Override
    @Transactional
    public UserVo register(RegisterRequest request, String timeZone, String language) {
        ZoneId zone = validZone(timeZone);
        String email = normalizeEmail(request.getEmail());
        checkLanguage(language);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCodeEnum.AUTH_PASSWORD_MISMATCH);
        }
        UserEntity existing = userMapper.selectByEmail(email);
        if (existing != null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_EMAIL_ALREADY_EXISTS);
        }
        consumeVerified(email, VerificationPurposeEnum.REGISTER.getValue());
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity().setId(IdWorker.getId()).setEmail(email).setPasswordHash(passwordEncoder.encode(request.getPassword())).setTimeZone(zone.getId()).setPreferredLanguage(language).setStatus(UserStatusEnum.ACTIVE.getValue()).setPasswordUpdateTime(now).setCreateTime(now).setUpdateTime(now);
        userMapper.insert(user);
        storageMapper.insert(new UserStorageAccountEntity().setId(IdWorker.getId()).setUserId(user.getId()).setUsedBytes(0L).setReservedBytes(0L).setLockVersion(0L).setCreateTime(now).setUpdateTime(now));
        return new UserVo(user.getId(), user.getEmail(), user.getTimeZone(), user.getPreferredLanguage());
    }

    @Override
    public AuthTokenVo login(LoginRequest request) {
        UserEntity user = userMapper.selectByEmail(normalizeEmail(request.getEmail()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS);
        }
        if (!UserStatusEnum.ACTIVE.getValue().equals(user.getStatus())) {
            throw new AuthenticationException(ErrorCodeEnum.AUTH_ACCESS_DENIED);
        }
        userMapper.updateLastLogin(user.getId());
        return new AuthTokenVo(jwtTokenService.issue(user.getId(), user.getTimeZone()), "Bearer", 7200);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCodeEnum.AUTH_PASSWORD_MISMATCH);
        }
        consumeVerified(email, VerificationPurposeEnum.RESET_PASSWORD.getValue());
        UserEntity user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS);
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getPassword()));
    }

    @Override
    public void logout() {
        UserContextHolderUtil.clear();
    }

    /**
     * 消费已验证验证码标记，防止验证码重复使用。
     *
     * @param email 规范化邮箱地址
     * @param purpose 验证码用途
     */
    private void consumeVerified(String email, String purpose) {
        String k = key(email, purpose) + ":verified";
        if (!Boolean.TRUE.equals(redis.hasKey(k))) {
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_NOT_VERIFIED);
        }
        redis.delete(k);
    }

    /**
     * 去除邮箱首尾空格并统一转换为小写。
     *
     * @param email 规范化邮箱地址
     * @return 规范化后的邮箱地址
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 根据邮箱和验证码用途生成Redis缓存键。
     *
     * @param email 规范化邮箱地址
     * @param purpose 验证码用途
     * @return 规范化后的邮箱地址
     */
    private String key(String email, String purpose) {
        return "auth:code:" + purpose + ":" + email;
    }

    /**
     * 校验并解析有效的IANA时区标识。
     *
     * @param value 待校验的IANA时区标识
     * @return 规范化后的邮箱地址
     */
    private ZoneId validZone(String value) {
        try {
            if (value == null || value.isBlank() || !ZoneId.getAvailableZoneIds().contains(value)) {
                throw new Exception();
            }
            return ZoneId.of(value);
        } catch (Exception e) {
            throw new RequestParameterException(ErrorCodeEnum.AUTH_TIME_ZONE_INVALID);
        }
    }

    /**
     * 校验请求语言是否为系统支持的语言。
     *
     * @param language 请求语言代码
     */
    private void checkLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new RequestParameterException(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED);
        }
        for (LanguageEnum item : LanguageEnum.values()) {
            if (item.getValue().equalsIgnoreCase(language)) {
                return;
            }
        }
        throw new RequestParameterException(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED);
    }
}
