package com.app.support.notification;

/**
 * 通知发送器接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface NotificationSender {
    /**
     * 发送一条用户通知。
     *
     * @param recipient 通知接收地址或接收人标识
     * @param subject 通知标题
     * @param content 通知正文
     */
    void send(String recipient, String subject, String content);
}
