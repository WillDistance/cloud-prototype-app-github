package com.app.task;

import com.app.enums.EntitlementStatusEnum;
import com.app.mapper.StorageEntitlementMapper;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.service.EntitlementCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权益到期清理定时任务。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Component
public class EntitlementCleanupTask {
    @Autowired
    private StorageEntitlementMapper entitlementMapper;
    @Autowired
    private EntitlementCleanupService entitlementCleanupService;

    /**
     * 定时触发已到期权益处理，具体清理事务交由独立业务服务执行。
     */
    @Scheduled(fixedDelayString = "${entitlement.cleanup.fixed-delay:60000}")
    public void cleanupExpiredEntitlements() {
        List<StorageEntitlementEntity> expired = entitlementMapper.selectExpired(LocalDateTime.now());
        for (StorageEntitlementEntity entitlement : expired) {
            entitlementCleanupService.cleanup(entitlement);
        }
    }
}
