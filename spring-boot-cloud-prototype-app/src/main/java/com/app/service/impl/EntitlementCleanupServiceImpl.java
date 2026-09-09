package com.app.service.impl;

import com.app.enums.DeleteReasonEnum;
import com.app.enums.EntitlementStatusEnum;
import com.app.enums.NotificationTypeEnum;
import com.app.enums.PhotoFileStatusEnum;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.service.EntitlementCleanupService;
import com.app.service.NotificationService;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权益到期清理业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class EntitlementCleanupServiceImpl extends ServiceImpl<StorageEntitlementMapper, StorageEntitlementEntity> implements EntitlementCleanupService {
    @Autowired
    private StorageEntitlementMapper entitlementMapper;
    @Autowired
    private PhotoFileMapper photoFileMapper;
    @Autowired
    private UserStorageAccountMapper storageAccountMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;


    @Override
    @Transactional
    public void cleanup(StorageEntitlementEntity entitlement) {
        if (entitlementMapper.markExpired(entitlement.getId(), EntitlementStatusEnum.EXPIRED.getValue()) != 1) {
            return;
        }
        UserStorageAccountEntity account = storageAccountMapper.selectByUserIdForUpdate(entitlement.getUserId());
        if (account == null) {
            return;
        }
        long capacity = entitlementMapper.sumActiveCapacity(entitlement.getUserId(), LocalDateTime.now());
        sendNotification(entitlement.getUserId(), NotificationTypeEnum.CLEANUP_STARTED, "存储空间清理开始");
        List<PhotoFileEntity> photos = photoFileMapper.selectAvailableOriginals(entitlement.getUserId());
        for (PhotoFileEntity photo : photos) {
            if (account.getUsedBytes() <= capacity) {
                break;
            }
            deletePhotoGroup(photo);
        }
        entitlementMapper.markProcessed(entitlement.getId(), LocalDateTime.now());
        sendNotification(entitlement.getUserId(), NotificationTypeEnum.CLEANUP_COMPLETED, "存储空间清理完成");
    }

    /**
     * 将照片文件组标记为待删除，实际对象删除由上传扫描定时任务异步执行。
     *
     * @param original 照片原图记录
     * @return 是否成功标记完整文件组
     */
    private boolean deletePhotoGroup(PhotoFileEntity original) {
        List<PhotoFileEntity> group = photoFileMapper.selectPhotoGroup(original.getUserId(), original.getObjectKey());
        if (group.isEmpty()) {
            return false;
        }
        for (PhotoFileEntity file : group) {
            photoFileMapper.updateDeleteStatus(file.getId(), PhotoFileStatusEnum.DELETE_PENDING.getValue(), DeleteReasonEnum.ENTITLEMENT_EXPIRED.getValue());
        }
        return true;
    }

    /**
     * 创建待发送的清理通知，不在清理事务中直接发送外部消息。
     *
     * @param userId 通知用户ID
     * @param type 通知类型
     * @param content 通知正文
     */
    private void sendNotification(Long userId, NotificationTypeEnum type, String content) {
        UserEntity user = userMapper.selectById(userId);
        if (user != null) {
            notificationService.create(userId, user.getPreferredLanguage(), type, user.getEmail(), type.getName(), content);
        }
    }
}
