package com.app.enums;

/**
 * 通知发送状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum NotificationStatusEnum {
    PENDING("PENDING", "待发送"),
    SENDING("SENDING", "发送中"),
    SENT("SENT", "发送成功"),
    FAILED("FAILED", "发送失败"),
    CANCELLED("CANCELLED", "已取消");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    NotificationStatusEnum(String value, String name) {
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
