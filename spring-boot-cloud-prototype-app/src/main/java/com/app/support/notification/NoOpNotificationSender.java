package com.app.support.notification;

import org.springframework.stereotype.Component;

/**
 * 不连接外部通知服务的本地通知发送器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Component
public class NoOpNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String subject, String content) {
        // 本地环境不发送外部通知，正式环境替换为实际通知供应商。
    }
}
