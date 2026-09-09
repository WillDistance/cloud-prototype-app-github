package com.app.constants;

/**
 * Redis键格式常量。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public final class RedisKeyConstants {
    /** 验证码缓存键前缀。 */
    public static final String AUTH_CODE_PREFIX = "auth:code:";

    /** 验证码发送冷却时间键后缀。 */
    public static final String AUTH_CODE_COOLDOWN_SUFFIX = ":cooldown";

    /** 验证码已验证状态键后缀。 */
    public static final String AUTH_CODE_VERIFIED_SUFFIX = ":verified";

    private RedisKeyConstants() {
    }
}
