package com.app.service;

import com.app.enums.NotificationTypeEnum;
import com.app.pojo.entity.NotificationEntity;

/**
 * 通知记录业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface NotificationService {
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
    NotificationEntity create(Long userId, String languageCode, NotificationTypeEnum type, String recipient, String subject, String content);
}
