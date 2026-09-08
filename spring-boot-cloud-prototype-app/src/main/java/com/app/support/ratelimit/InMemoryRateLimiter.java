package com.app.support.ratelimit;

import com.app.records.Window;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单机限流实现；集群部署可替换为Redis实现。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class InMemoryRateLimiter implements RateLimiter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        if (key == null || key.isBlank() || limit <= 0 || window == null || window.isNegative() || window.isZero()) {
            return false;
        }
        long now = System.nanoTime();
        long nanos = window.toNanos();
        Window current = windows.compute(key, (ignored, old) ->
                old == null || now - old.startedAt() >= nanos ? new Window(now, new AtomicInteger()) : old);
        synchronized (current) {
            if (current.count().get() >= limit) {
                return false;
            }
            current.count().incrementAndGet();
            return true;
        }
    }
}
