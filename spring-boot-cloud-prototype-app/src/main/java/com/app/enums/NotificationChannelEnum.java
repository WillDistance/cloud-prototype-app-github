package com.app.enums;

/**
 * 通知发送渠道
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum NotificationChannelEnum {
    EMAIL("EMAIL", "电子邮件"),
    IN_APP("IN_APP", "应用内通知");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    NotificationChannelEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
