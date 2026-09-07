package com.app.support;


import com.app.support.ratelimit.InMemoryRateLimiter;
import com.app.support.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 限流器并发回归测试。
 *
 * @author yanlei
 * @since 2026-09-06
 */
class InMemoryRateLimiterTest {
    @Test
    void shouldEnforceCapacityUnderConcurrency() throws Exception {
        RateLimiter limiter = new InMemoryRateLimiter();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();
        for (int i = 0; i < 32; i++) {
            executor.submit(() -> {
                start.await();
                if (limiter.tryAcquire("auth:ip:127.0.0.1", 5, Duration.ofMinutes(1))) allowed.incrementAndGet();
                return null;
            });
        }
        start.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) Thread.yield();
        assertEquals(5, allowed.get());
        assertFalse(limiter.tryAcquire("", 1, Duration.ofMinutes(1)));
        assertTrue(limiter.tryAcquire("other", 1, Duration.ofMinutes(1)));
    }
}
