package com.app.support.notification;

import com.app.enums.NotificationChannelEnum;

import java.util.Map;

/**
 * 通知发送消息，不包含日志输出职责。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public record NotificationMessage(NotificationChannelEnum channel, String recipient,
                                  String subject, String content, Map<String, Object> variables) {
}
