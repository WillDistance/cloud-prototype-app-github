package com.app.records;

/**
 * 已认证用户上下文
 *
 * @author yanlei
 * @since 2026-09-06
 */
public record AuthenticatedUser(Long userId, String timeZone, String tokenId) {
}
