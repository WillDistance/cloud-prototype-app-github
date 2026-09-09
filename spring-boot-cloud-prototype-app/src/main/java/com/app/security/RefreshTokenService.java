package com.app.security;

import cn.hutool.crypto.digest.DigestUtil;
import com.app.constants.RedisKeyConstants;
import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Refresh Token会话生命周期管理服务。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class RefreshTokenService {
    private static final int TOKEN_BYTES = 48;
    private final SecureRandom secureRandom = new SecureRandom();
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${security.refresh-token.ttl:PT30D}")
    private Duration refreshTokenTtl;

    /**
     * 创建Refresh Token并保存其服务端会话记录。
     *
     * @param userId 登录用户ID
     * @return Refresh Token及其会话信息
     */
    public IssuedRefreshToken issue(Long userId) {
        String token = randomToken();
        String sessionId = UUID.randomUUID().toString();
        String family = UUID.randomUUID().toString();
        RefreshTokenSession session = new RefreshTokenSession(userId, sessionId, family, hash(token), Instant.now().plus(refreshTokenTtl));
        save(session);
        return new IssuedRefreshToken(token, session);
    }

    /**
     * 校验并轮换Refresh Token，旧令牌验证成功后立即失效。
     *
     * @param token 客户端提交的Refresh Token
     * @return 新Refresh Token及其会话信息
     */
    public IssuedRefreshToken rotate(String token) {
        RefreshTokenSession current = loadByToken(token);
        if (current == null || current.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationException(ErrorCodeEnum.AUTH_TOKEN_EXPIRED);
        }
        redis.delete(key(current.getTokenHash()));
        return issueWithFamily(current.getUserId(), current.getTokenFamily());
    }

    /**
     * 撤销Refresh Token对应的登录会话。
     *
     * @param token 当前登录会话的Refresh Token
     */
    public void revoke(String token) {
        RefreshTokenSession session = loadByToken(token);
        if (session != null) {
            redis.delete(key(session.getTokenHash()));
        }
    }

    /**
     * 生成不可预测的Refresh Token。
     *
     * @return URL安全的随机Refresh Token
     */
    private String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 为指定令牌家族创建新的会话记录。
     *
     * @param userId 用户ID
     * @param family 令牌家族编号
     * @return 新Refresh Token及其会话信息
     */
    private IssuedRefreshToken issueWithFamily(Long userId, String family) {
        String token = randomToken();
        RefreshTokenSession session = new RefreshTokenSession(userId, UUID.randomUUID().toString(), family, hash(token), Instant.now().plus(refreshTokenTtl));
        save(session);
        return new IssuedRefreshToken(token, session);
    }

    /**
     * 从Redis读取并匹配Refresh Token摘要。
     *
     * @param token 客户端提交的Refresh Token
     * @return 匹配的会话，不存在或摘要不匹配时返回null
     */
    private RefreshTokenSession loadByToken(String token) {
        return read(redis.opsForValue().get(key(hash(token))));
    }

    /**
     * 保存Refresh Token会话并设置过期时间。
     *
     * @param session 待保存的Refresh Token会话
     */
    private void save(RefreshTokenSession session) {
        try {
            redis.opsForValue().set(key(session.getTokenHash()), objectMapper.writeValueAsString(session), refreshTokenTtl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Refresh Token会话序列化失败", exception);
        }
    }

    /**
     * 解析Redis中的Refresh Token会话JSON。
     *
     * @param json Redis会话JSON
     * @return Refresh Token会话，JSON为空或格式错误时返回null
     */
    private RefreshTokenSession read(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, RefreshTokenSession.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    /**
     * 计算Refresh Token摘要，避免Redis保存令牌明文。
     *
     * @param token Refresh Token明文
     * @return Refresh Token SHA-256摘要
     */
    private String hash(String token) {
        return DigestUtil.sha256Hex(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据Refresh Token摘要生成Redis会话键。
     *
     * @param tokenHash Refresh Token摘要
     * @return Refresh Token会话键
     */
    private String key(String tokenHash) {
        return RedisKeyConstants.REFRESH_TOKEN_PREFIX + tokenHash;
    }

    /** Refresh Token签发结果。 */
    public record IssuedRefreshToken(String token, RefreshTokenSession session) {
    }
}
