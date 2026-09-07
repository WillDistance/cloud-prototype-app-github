package com.app.support.ratelimit;

import java.time.Duration;

/**
 * 可替换的请求限流抽象。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface RateLimiter {
    /**
     * 在时间窗口内尝试消耗一次配额。
     *
     * @param key    限流键，不得包含密码、验证码或令牌
     * @param limit  窗口内最大次数
     * @param window 时间窗口
     * @return 是否允许本次请求
     */
    boolean tryAcquire(String key, int limit, Duration window);
}
