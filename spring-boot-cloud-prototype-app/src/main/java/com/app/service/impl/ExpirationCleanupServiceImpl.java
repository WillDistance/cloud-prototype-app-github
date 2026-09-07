package com.app.service.impl;

import com.app.enums.DeleteReasonEnum;
import com.app.enums.PhotoStatusEnum;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.PhotoMapper;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.PhotoEntity;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.service.ExpirationCleanupService;
import com.app.support.cleanup.ExpirationCleanupLock;
import com.app.support.cleanup.ExpirationCleanupNotificationPublisher;
import com.app.support.objectstorage.ObjectStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权益到期后的容量重算和照片清理服务。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class ExpirationCleanupServiceImpl implements ExpirationCleanupService {
    private static final int BATCH_SIZE = 100;
    private final StorageEntitlementMapper entitlementMapper;
    private final PhotoMapper photoMapper;
    private final PhotoFileMapper fileMapper;
    private final UserStorageAccountMapper accountMapper;
    private final ObjectStorageService storageService;
    private final ExpirationCleanupLock cleanupLock;
    private final ExpirationCleanupNotificationPublisher notifications;
    private final Clock clock;

    @Autowired
    public ExpirationCleanupServiceImpl(StorageEntitlementMapper entitlementMapper, PhotoMapper photoMapper,
                                        PhotoFileMapper fileMapper, UserStorageAccountMapper accountMapper,
                                        ObjectStorageService storageService, ExpirationCleanupLock cleanupLock,
                                        ExpirationCleanupNotificationPublisher notifications) {
        this(entitlementMapper, photoMapper, fileMapper, accountMapper, storageService, cleanupLock, notifications,
                Clock.systemUTC());
    }

    public ExpirationCleanupServiceImpl(StorageEntitlementMapper entitlementMapper, PhotoMapper photoMapper,
                                        PhotoFileMapper fileMapper, UserStorageAccountMapper accountMapper,
                                        ObjectStorageService storageService, ExpirationCleanupLock cleanupLock,
                                        ExpirationCleanupNotificationPublisher notifications, Clock clock) {
        this.entitlementMapper = entitlementMapper;
        this.photoMapper = photoMapper;
        this.fileMapper = fileMapper;
        this.accountMapper = accountMapper;
        this.storageService = storageService;
        this.cleanupLock = cleanupLock;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Override
    @Scheduled(fixedDelayString = "${storage.expiration-cleanup.fixed-delay:60000}")
    public void runCleanup() {
        cleanupLock.execute(() -> {
            LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
            List<StorageEntitlementEntity> pending = entitlementMapper.selectPendingExpiration(now, BATCH_SIZE);
            Map<Long, String> users = new HashMap<>();
            for (StorageEntitlementEntity entitlement : pending) {
                users.putIfAbsent(entitlement.getUserId(), entitlement.getUserTimeZoneSnapshot());
            }
            users.forEach((userId, zone) -> processUser(userId, zone, now));
            return null;
        });
    }

    @Transactional
    protected void processUser(Long userId, String zone, LocalDateTime now) {
        notifications.started(userId, zone, now);
        UserStorageAccountEntity account = accountMapper.selectByUserIdForUpdate(userId);
        if (account == null) return;
        long capacity = value(entitlementMapper.sumActiveCapacity(userId, now));
        long used = value(account.getUsedBytes());
        boolean deleted = false;
        if (used > capacity) {
            List<PhotoEntity> photos = photoMapper.selectList(new LambdaQueryWrapper<PhotoEntity>()
                    .eq(PhotoEntity::getUserId, userId).in(PhotoEntity::getStatus, PhotoStatusEnum.AVAILABLE,
                            PhotoStatusEnum.DELETE_PENDING, PhotoStatusEnum.DELETE_FAILED)
                    .orderByAsc(PhotoEntity::getUploadedTime).orderByAsc(PhotoEntity::getId));
            for (PhotoEntity photo : photos) {
                if (used <= capacity) break;
                if (!deleteGroup(photo, userId)) return;
                used -= value(photo.getOriginalSizeBytes());
                deleted = true;
            }
        }
        if (used <= capacity) {
            for (StorageEntitlementEntity entitlement : entitlementMapper.selectPendingExpiration(now, BATCH_SIZE)) {
                if (userId.equals(entitlement.getUserId()) && entitlementMapper.markExpiredProcessed(entitlement.getId(), now) == 1) {
                    entitlement.setStatus(com.app.enums.EntitlementStatusEnum.EXPIRED).setExpiredProcessedTime(now);
                }
            }
            notifications.completed(userId, zone, now, deleted);
        }
    }

    /**
     * 删除一组照片文件并更新存储使用量。
     *
     * @param photo  照片记录
     * @param userId 用户ID
     * @return 操作是否成功
     */
    private boolean deleteGroup(PhotoEntity photo, Long userId) {
        List<PhotoFileEntity> files = fileMapper.selectList(new LambdaQueryWrapper<PhotoFileEntity>().eq(PhotoFileEntity::getPhotoId, photo.getId()));
        photo.setStatus(PhotoStatusEnum.DELETE_PENDING);
        photoMapper.updateById(photo);
        boolean failed = false;
        for (PhotoFileEntity file : files) {
            file.setStatus(PhotoStatusEnum.DELETE_PENDING).setDeleteReason(DeleteReasonEnum.ENTITLEMENT_EXPIRED);
            fileMapper.updateById(file);
            try {
                storageService.delete(file.getObjectKey());
                fileMapper.deleteById(file.getId());
            } catch (RuntimeException exception) {
                failed = true;
                file.setStatus(PhotoStatusEnum.DELETE_FAILED);
                fileMapper.updateById(file);
            }
        }
        if (failed) {
            photo.setStatus(PhotoStatusEnum.DELETE_FAILED);
            photoMapper.updateById(photo);
            return false;
        }
        return accountMapper.decreaseUsedBytes(userId, value(photo.getOriginalSizeBytes())) == 1;
    }

    /**
     * 将可选的数值转换为非空的容量或数量值。
     *
     * @param value 权益时长数值
     * @return 方法处理后的结果
     */
    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
