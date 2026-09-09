package com.app.constants;

/**
 * Redis键名及键后缀常量，集中维护项目使用的Redis数据结构。
 *
 * @author yanlei
 * @since 2026-09-07
 */
public final class RedisKeyConstants {
    /** 认证验证码键前缀。 */
    public static final String AUTH_CODE_PREFIX = "auth:code:";

    /** 验证码发送冷却键后缀。 */
    public static final String AUTH_CODE_COOLDOWN_SUFFIX = ":cooldown";

    /** 验证码已验证标记键后缀。 */
    public static final String AUTH_CODE_VERIFIED_SUFFIX = ":verified";

    /** 按来源IP限流键的分隔后缀。 */
    public static final String RATE_LIMIT_IP_SUFFIX = ":ip:";

    private RedisKeyConstants() {
    }
}
