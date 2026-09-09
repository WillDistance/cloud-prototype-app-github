package com.app.service;

import com.app.pojo.entity.StorageEntitlementEntity;

/**
 * 权益到期清理业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface EntitlementCleanupService {
    /**
     * 在事务中处理单个已到期权益及其超额照片。
     *
     * @param entitlement 已到期存储权益
     */
    void cleanup(StorageEntitlementEntity entitlement);
}
