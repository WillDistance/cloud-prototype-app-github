package com.app.security;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWTUtil;
import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import com.app.records.AuthenticatedUser;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT令牌签发与验证服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class JwtTokenService {
    private final byte[] secret;
    private final Duration ttl;

    /**
     * 创建JWT令牌服务并校验签名密钥和令牌有效期配置。
     *
     * @param secret JWT签名密钥，至少需要32个字符
     * @param ttl JWT令牌有效期
     */
    public JwtTokenService(String secret, Duration ttl) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET至少需要32个字符");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalStateException("JWT_TTL必须为正数");
        }
        this.secret = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.ttl = ttl;
    }

    /**
     * 根据用户身份和时区签发JWT令牌。
     *
     * @param userId 用户ID
     * @param timeZone 用户保存的IANA时区
     * @return 已签名的JWT字符串
     */
    public String issue(Long userId, String timeZone) {
        return issue(userId, timeZone, null);
    }

    /**
     * 根据用户身份、时区和登录会话签发Access Token。
     *
     * @param userId 用户ID
     * @param timeZone 用户保存的IANA时区
     * @param sessionId Refresh Token会话ID
     * @return 已签名的Access Token
     */
    public String issue(Long userId, String timeZone, String sessionId) {
        long issuedAt = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>();
        payload.put(JWT.SUBJECT, String.valueOf(userId));
        payload.put("tz", timeZone);
        payload.put(JWT.JWT_ID, UUID.randomUUID().toString());
        payload.put("typ", "access");
        if (sessionId != null) {
            payload.put("sid", sessionId);
        }
        payload.put(JWT.ISSUED_AT, issuedAt);
        payload.put(JWT.EXPIRES_AT, issuedAt + ttl.toSeconds());
        return JWTUtil.createToken(payload, secret);
    }

    /**
     * 使用签名密钥验证并解析JWT令牌，校验令牌载荷后构造认证用户上下文。
     *
     * @param token 待验证的JWT字符串
     * @return 已认证用户信息，包含用户ID、时区和令牌ID
     * @throws AuthenticationException 令牌签名无效、已过期或载荷不完整时抛出
     */
    public AuthenticatedUser parse(String token) {
        try {
            if (!JWTUtil.verify(token, secret)) {
                throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
            }
            JWT jwt = JWTUtil.parseToken(token);
            if (!"access".equals(String.valueOf(jwt.getPayload("typ")))) {
                throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
            }
            Long userId = Long.valueOf(String.valueOf(jwt.getPayload(JWT.SUBJECT)));
            String timeZone = String.valueOf(jwt.getPayload("tz"));
            String tokenId = String.valueOf(jwt.getPayload(JWT.JWT_ID));
            Object expiresAt = jwt.getPayload(JWT.EXPIRES_AT);
            if (!(expiresAt instanceof Number) || ((Number) expiresAt).longValue() <= System.currentTimeMillis() / 1000) {
                throw new AuthenticationException(ErrorCodeEnum.AUTH_TOKEN_EXPIRED);
            }
            if (userId <= 0 || timeZone.isBlank() || tokenId.isBlank()) {
                throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
            }
            return new AuthenticatedUser(userId, timeZone, tokenId);
        } catch (JWTException exception) {
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("expired")) {
                throw new AuthenticationException(ErrorCodeEnum.AUTH_TOKEN_EXPIRED);
            }
            throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
        } catch (AuthenticationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
        }
    }
}
