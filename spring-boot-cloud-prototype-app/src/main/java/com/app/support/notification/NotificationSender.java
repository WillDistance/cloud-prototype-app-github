package com.app.support.notification;

/**
 * 通知发送器抽象，隔离邮件或应用内通知基础设施。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface NotificationSender {
    /**
     * 发送通知。
     *
     * @param message 通知消息
     */
    void send(NotificationMessage message);
}
