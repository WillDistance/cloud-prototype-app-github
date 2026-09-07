package com.app.support.notification;

import org.springframework.stereotype.Component;

/**
 * 原型默认通知发送器，不输出通知敏感内容。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class NoOpNotificationSender implements NotificationSender {
    @Override
    public void send(NotificationMessage message) {
        // 生产环境由邮件或站内信适配器替换，禁止记录正文和敏感变量。
    }
}
