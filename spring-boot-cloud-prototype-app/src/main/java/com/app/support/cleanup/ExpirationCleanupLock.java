package com.app.support.cleanup;

/**
 * 清理任务互斥锁抽象，便于替换为Redis或单机实现。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@FunctionalInterface
public interface ExpirationCleanupLock {
    <T> T execute(Callback<T> callback);

    interface Callback<T> {
        T get();
    }
}
