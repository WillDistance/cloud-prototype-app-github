package com.app.service.impl;

import com.app.enums.NotificationChannelEnum;
import com.app.enums.NotificationStatusEnum;
import com.app.enums.NotificationTypeEnum;
import com.app.mapper.NotificationMapper;
import com.app.pojo.entity.NotificationEntity;
import com.app.service.NotificationService;
import com.app.utils.BusinessNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 通知记录服务。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 创建待发送的用户通知记录。
     *
     * @param userId 通知用户ID
     * @param languageCode 通知语言代码
     * @param type 通知类型
     * @param recipient 接收地址或接收人标识
     * @param subject 通知标题
     * @param content 通知正文
     * @return 已创建的通知记录
     */
    @Transactional
    public NotificationEntity create(Long userId, String languageCode, NotificationTypeEnum type, String recipient, String subject, String content) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId()).setNotificationNo(BusinessNumberGenerator.generate("NT"))
                .setUserId(userId).setNotificationType(type.getValue()).setLanguageCode(languageCode)
                .setChannel(NotificationChannelEnum.EMAIL.getValue()).setRecipient(recipient).setSubject(subject)
                .setContent(content).setStatus(NotificationStatusEnum.PENDING.getValue()).setRetryCount(0);
        entity.setCreateTime(LocalDateTime.now()).setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(entity);
        return entity;
    }
}
