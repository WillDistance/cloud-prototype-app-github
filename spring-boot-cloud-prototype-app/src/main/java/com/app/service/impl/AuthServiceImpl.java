package com.app.service.impl;

import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.mapper.EmailVerificationCodeMapper;
import com.app.mapper.UserMapper;
import com.app.pojo.dto.AuthDto;
import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.AuthLoginVo;
import com.app.pojo.vo.AuthRegisterVo;
import com.app.security.JwtTokenService;
import com.app.service.AuthService;
import com.app.support.verification.RegisterCodeCache;
import com.app.support.verification.VerificationCodeSender;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 认证服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserMapper userMapper;
    private final EmailVerificationCodeMapper codeMapper;
    private final RegisterCodeCache codeCache;
    private final VerificationCodeSender codeSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthServiceImpl(UserMapper userMapper, EmailVerificationCodeMapper codeMapper, RegisterCodeCache codeCache,
                           VerificationCodeSender codeSender, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.codeMapper = codeMapper;
        this.codeCache = codeCache;
        this.codeSender = codeSender;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean sendRegisterCode(AuthDto.SendRegisterCode dto, String requestIp) {
        String email = normalizeEmail(dto.getEmail());
        assertEmailUnused(email);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        log.info("Send register code to email: {}, code: {}", email, code);
        EmailVerificationCodeEntity entity = new EmailVerificationCodeEntity()
                .setEmail(email).setPurpose(VerificationPurposeEnum.REGISTER).setCodeHash(passwordEncoder.encode(code))
                .setStatus(VerificationStatusEnum.PENDING).setSendCount(1).setVerifyFailCount(0)
                .setExpireTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5)).setRequestIp(requestIp);
        codeMapper.insert(entity);
        if (!codeCache.requestCode(email, new RegisterCodeCache.CodeState(entity.getId(), entity.getCodeHash()))) {
            codeMapper.updateStatusIfCurrent(entity.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.INVALIDATED);
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_SEND_TOO_FREQUENT);
        }
        codeSender.sendRegisterCode(email, code, UserLanguageEnum.EN);
        return true;
    }

    @Override
    public boolean verifyRegisterCode(AuthDto.VerifyRegisterCode dto) {
        String email = normalizeEmail(dto.getEmail());
        RegisterCodeCache.VerifyResult result = codeCache.verifyCode(email, dto.getCode(), passwordEncoder);
        EmailVerificationCodeEntity record = latestPendingRecord(email);
        if (result == RegisterCodeCache.VerifyResult.VERIFIED) {
            if (record != null)
                codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.VERIFIED);
            return true;
        }
        if (record != null && result == RegisterCodeCache.VerifyResult.ATTEMPTS_EXCEEDED) {
            codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.INVALIDATED);
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_ATTEMPTS_EXCEEDED);
        }
        if (record != null && result == RegisterCodeCache.VerifyResult.EXPIRED) {
            codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.EXPIRED);
        }
        throw new BusinessException(ErrorCodeEnum.AUTH_CODE_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthRegisterVo register(AuthDto.Register dto, String timeZone, String acceptLanguage) {
        String email = normalizeEmail(dto.getEmail());
        ZoneId zoneId = parseZone(timeZone);
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new BusinessException(ErrorCodeEnum.AUTH_PASSWORD_MISMATCH);
        assertEmailUnused(email);
        EmailVerificationCodeEntity record = latestVerifiedRecord(email);
        if (record == null || record.getExpireTime().isBefore(LocalDateTime.now(ZoneOffset.UTC)) ||
                codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.VERIFIED, VerificationStatusEnum.INVALIDATED) != 1) {
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_NOT_VERIFIED);
        }
        UserLanguageEnum language = parseLanguage(acceptLanguage);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        UserEntity user = new UserEntity().setEmail(email).setPasswordHash(passwordEncoder.encode(dto.getPassword()))
                .setPasswordAlgorithm("ARGON2ID").setTimeZone(zoneId.getId()).setPreferredLanguage(language)
                .setStatus(UserStatusEnum.ACTIVE).setPasswordUpdateTime(now);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCodeEnum.AUTH_EMAIL_ALREADY_EXISTS);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    codeCache.consumeVerified(email);
                }
            });
        } else {
            codeCache.consumeVerified(email);
        }
        return new AuthRegisterVo().setUserId(user.getId()).setEmail(email).setTimeZone(zoneId.getId()).setPreferredLanguage(language.getValue());
    }

    @Override
    public AuthLoginVo login(AuthDto.Login dto) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, normalizeEmail(dto.getEmail())).last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash()))
            throw new BusinessException(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS);
        if (user.getStatus() != UserStatusEnum.ACTIVE) throw new BusinessException(ErrorCodeEnum.AUTH_ACCESS_DENIED);
        user.setLastLoginTime(LocalDateTime.now(ZoneOffset.UTC));
        userMapper.updateById(user);
        return new AuthLoginVo().setAccessToken(jwtTokenService.issue(user.getId(), user.getTimeZone())).setTokenType("Bearer").setExpiresIn(7200L);
    }

    @Override
    public boolean sendResetPasswordCode(AuthDto.SendResetPasswordCode dto, String requestIp) {
        String email = normalizeEmail(dto.getEmail());
        UserEntity user = findUser(email);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        EmailVerificationCodeEntity entity = new EmailVerificationCodeEntity().setEmail(email).setUserId(user.getId())
                .setPurpose(VerificationPurposeEnum.RESET_PASSWORD).setCodeHash(passwordEncoder.encode(code))
                .setStatus(VerificationStatusEnum.PENDING).setSendCount(1).setVerifyFailCount(0)
                .setExpireTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5)).setRequestIp(requestIp);
        codeMapper.insert(entity);
        if (!codeCache.requestCode(resetCacheKey(email), new RegisterCodeCache.CodeState(entity.getId(), entity.getCodeHash()))) {
            codeMapper.updateStatusIfCurrent(entity.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.INVALIDATED);
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_SEND_TOO_FREQUENT);
        }
        codeSender.sendRegisterCode(email, code, user.getPreferredLanguage());
        return true;
    }

    @Override
    public boolean verifyResetPasswordCode(AuthDto.VerifyResetPasswordCode dto) {
        String email = normalizeEmail(dto.getEmail());
        RegisterCodeCache.VerifyResult result = codeCache.verifyCode(resetCacheKey(email), dto.getCode(), passwordEncoder);
        EmailVerificationCodeEntity record = latestRecord(email, VerificationPurposeEnum.RESET_PASSWORD, VerificationStatusEnum.PENDING);
        if (result == RegisterCodeCache.VerifyResult.VERIFIED) {
            if (record != null)
                codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.VERIFIED);
            return true;
        }
        if (record != null && result == RegisterCodeCache.VerifyResult.ATTEMPTS_EXCEEDED) {
            codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.INVALIDATED);
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_ATTEMPTS_EXCEEDED);
        }
        if (record != null && result == RegisterCodeCache.VerifyResult.EXPIRED)
            codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.PENDING, VerificationStatusEnum.EXPIRED);
        throw new BusinessException(ErrorCodeEnum.AUTH_CODE_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(AuthDto.ResetPassword dto) {
        String email = normalizeEmail(dto.getEmail());
        if (!dto.getNewPassword().equals(dto.getConfirmPassword()))
            throw new BusinessException(ErrorCodeEnum.AUTH_PASSWORD_MISMATCH);
        UserEntity user = findUser(email);
        EmailVerificationCodeEntity record = latestRecord(email, VerificationPurposeEnum.RESET_PASSWORD, VerificationStatusEnum.VERIFIED);
        if (record == null || record.getExpireTime().isBefore(LocalDateTime.now(ZoneOffset.UTC)) ||
                codeMapper.updateStatusIfCurrent(record.getId(), VerificationStatusEnum.VERIFIED, VerificationStatusEnum.INVALIDATED) != 1 ||
                !codeCache.consumeVerified(resetCacheKey(email)))
            throw new BusinessException(ErrorCodeEnum.AUTH_CODE_NOT_VERIFIED);
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword())).setPasswordAlgorithm("ARGON2ID")
                .setPasswordUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
        userMapper.updateById(user);
        return true;
    }

    @Override
    public boolean logout(String tokenId) {
        // 当前采用无状态JWT：客户端删除令牌，服务端不维护黑名单，令牌在到期前仍有效。
        return tokenId != null && !tokenId.isBlank();
    }

    /**
     * 校验邮箱尚未注册。
     *
     * @param email 邮箱地址
     */
    private void assertEmailUnused(String email) {
        if (userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, email)) > 0)
            throw new BusinessException(ErrorCodeEnum.AUTH_EMAIL_ALREADY_EXISTS);
    }

    /**
     * 查询指定邮箱最新的待验证注册验证码记录。
     *
     * @param email 邮箱地址
     * @return 方法处理后的结果
     */
    private EmailVerificationCodeEntity latestPendingRecord(String email) {
        return latestRecord(email, VerificationPurposeEnum.REGISTER, VerificationStatusEnum.PENDING);
    }

    /**
     * 查询指定邮箱最新的已验证注册验证码记录。
     *
     * @param email 邮箱地址
     * @return 方法处理后的结果
     */
    private EmailVerificationCodeEntity latestVerifiedRecord(String email) {
        return latestRecord(email, VerificationPurposeEnum.REGISTER, VerificationStatusEnum.VERIFIED);
    }

    /**
     * 按邮箱、用途和状态查询最新验证码记录。
     *
     * @param email   邮箱地址
     * @param purpose 验证码用途
     * @param status  验证码状态
     * @return 方法处理后的结果
     */
    private EmailVerificationCodeEntity latestRecord(String email, VerificationPurposeEnum purpose, VerificationStatusEnum status) {
        return codeMapper.selectOne(new LambdaQueryWrapper<EmailVerificationCodeEntity>().eq(EmailVerificationCodeEntity::getEmail, email)
                .eq(EmailVerificationCodeEntity::getPurpose, purpose).eq(EmailVerificationCodeEntity::getStatus, status).orderByDesc(EmailVerificationCodeEntity::getId).last("LIMIT 1"));
    }

    /**
     * 按邮箱查询处于有效状态的用户。
     *
     * @param email 邮箱地址
     * @return 方法处理后的结果
     */
    private UserEntity findUser(String email) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, email).last("LIMIT 1"));
        if (user == null || user.getStatus() != UserStatusEnum.ACTIVE)
            throw new BusinessException(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS);
        return user;
    }

    /**
     * 生成密码重置验证码的缓存键。
     *
     * @param email 邮箱地址
     * @return 方法处理后的结果
     */
    private String resetCacheKey(String email) {
        return "reset:" + email;
    }

    /**
     * 校验并规范化邮箱地址，用作查询键。
     *
     * @param email 邮箱地址
     * @return 方法处理后的结果
     */
    private String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalized).matches() || normalized.length() > 320) {
            throw new BusinessException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        return normalized;
    }

    /**
     * 校验并解析用户时区。
     *
     * @param zone 用户时区
     * @return 方法处理后的结果
     */
    private ZoneId parseZone(String zone) {
        if (zone == null || zone.isBlank() || !ZoneId.getAvailableZoneIds().contains(zone))
            throw new BusinessException(ErrorCodeEnum.AUTH_TIME_ZONE_INVALID);
        return ZoneId.of(zone);
    }

    /**
     * 解析请求语言并转换为系统语言枚举。
     *
     * @param header 语言请求头
     * @return 方法处理后的结果
     */
    private UserLanguageEnum parseLanguage(String header) {
        if (header == null) return UserLanguageEnum.EN;
        String tag = header.split(",")[0].trim();
        if (tag.equalsIgnoreCase("zh-CN") || tag.toLowerCase(Locale.ROOT).startsWith("zh-"))
            return UserLanguageEnum.ZH_CN;
        if (tag.equalsIgnoreCase("de") || tag.toLowerCase(Locale.ROOT).startsWith("de-")) return UserLanguageEnum.DE;
        if (tag.equalsIgnoreCase("en") || tag.toLowerCase(Locale.ROOT).startsWith("en-")) return UserLanguageEnum.EN;
        return UserLanguageEnum.EN;
    }
}
