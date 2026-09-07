package com.app.service;

import com.app.enums.NotificationChannelEnum;
import com.app.enums.NotificationTypeEnum;
import com.app.pojo.entity.NotificationEntity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 多语言通知业务服务。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface NotificationService {
    /**
     * 创建幂等通知记录。
     *
     * @param userId         用户ID
     * @param recipient      通知接收地址
     * @param type           通知类型
     * @param channel        通知渠道
     * @param idempotencyKey 通知幂等键
     * @param variables      通知模板变量
     * @return 已创建或已存在的通知记录
     */
    NotificationEntity create(Long userId, String recipient, NotificationTypeEnum type,
                              NotificationChannelEnum channel, String idempotencyKey,
                              Map<String, Object> variables);

    /**
     * 发送指定通知。
     *
     * @param notificationId 通知ID
     * @param clock          时间时钟
     */
    void send(Long notificationId, Clock clock);

    /**
     * 发送指定通知。
     *
     * @param notificationId 通知ID
     * @param now            当前UTC时间
     */
    void send(Long notificationId, LocalDateTime now);
}
