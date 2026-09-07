package com.app.security;

/**
 * 请求级用户上下文持有器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class UserContextHolder {
    private static final ThreadLocal<AuthenticatedUser> CONTEXT = new ThreadLocal<>();

    /**
     * 处理UserContextHolder相关的业务逻辑。
     *
     * @return 处理结果
     */
    private UserContextHolder() {
    }

    public static void set(AuthenticatedUser user) {
        CONTEXT.set(user);
    }

    public static AuthenticatedUser get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
