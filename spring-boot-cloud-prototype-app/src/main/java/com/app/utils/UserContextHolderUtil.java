package com.app.utils;

import com.app.records.AuthenticatedUser;

import java.time.ZoneId;

/**
 * 请求级用户上下文持有器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class UserContextHolderUtil {
    private static final ThreadLocal<AuthenticatedUser> CONTEXT = new ThreadLocal<>();

    public static void set(AuthenticatedUser user) {
        CONTEXT.set(user);
    }

    public static AuthenticatedUser get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        return get().userId();
    }

    public static ZoneId getZoneId() {
        return ZoneId.of(get().timeZone());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
