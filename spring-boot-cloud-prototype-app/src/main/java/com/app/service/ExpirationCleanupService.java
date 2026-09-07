package com.app.service;

/**
 * 权益到期清理服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface ExpirationCleanupService {
    /**
     * 执行存储权益到期及超额照片清理任务。
     */
    void runCleanup();
}
