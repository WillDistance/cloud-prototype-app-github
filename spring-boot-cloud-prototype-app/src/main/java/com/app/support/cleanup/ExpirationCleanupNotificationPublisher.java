package com.app.support.cleanup;

import java.time.LocalDateTime;

/**
 * 到期清理通知边界，具体发送由后续通知模块实现。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface ExpirationCleanupNotificationPublisher {
    default void started(Long userId, String timeZone, LocalDateTime now) {
    }

    default void completed(Long userId, String timeZone, LocalDateTime now, boolean deleted) {
    }
}
