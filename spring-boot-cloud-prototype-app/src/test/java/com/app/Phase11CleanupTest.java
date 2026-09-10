package com.app;

import com.app.enums.NotificationTypeEnum;
import com.app.enums.PhotoFileStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 阶段11权益到期清理和通知契约测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase11CleanupTest {
    /**
     * 验证权益清理使用规定的通知类型。
     */
    @Test
    void shouldProvideCleanupNotificationTypes() {
        assertEquals("CLEANUP_STARTED", NotificationTypeEnum.CLEANUP_STARTED.getValue());
        assertEquals("CLEANUP_COMPLETED", NotificationTypeEnum.CLEANUP_COMPLETED.getValue());
    }

    /**
     * 验证照片清理状态仍使用可重试的删除状态。
     */
    @Test
    void shouldKeepDeleteFailureState() {
        assertEquals("DELETE_PENDING", PhotoFileStatusEnum.DELETE_PENDING.getValue());
        assertEquals("DELETE_FAILED", PhotoFileStatusEnum.DELETE_FAILED.getValue());
        assertEquals("DELETED", PhotoFileStatusEnum.DELETED.getValue());
    }
}
