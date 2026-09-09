package com.app.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Refresh Token服务端会话。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@NoArgsConstructor
public class RefreshTokenSession {
    private Long userId;
    private String sessionId;
    private String tokenFamily;
    private String tokenHash;
    private Instant expiresAt;

    /**
     * 创建Refresh Token会话。
     *
     * @param userId 用户ID
     * @param sessionId 登录会话ID
     * @param tokenFamily 令牌家族ID
     * @param tokenHash Refresh Token摘要
     * @param expiresAt 会话过期时间
     */
    public RefreshTokenSession(Long userId, String sessionId, String tokenFamily, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.tokenFamily = tokenFamily;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
