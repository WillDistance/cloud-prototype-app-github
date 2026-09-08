package com.app.enums;

/**
 * 通知类型
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum NotificationTypeEnum {
    VERIFY_CODE("VERIFY_CODE", "邮箱验证码"),
    EXPIRY_REMINDER("EXPIRY_REMINDER", "权益到期提醒"),
    CLEANUP_STARTED("CLEANUP_STARTED", "自动清理开始"),
    CLEANUP_COMPLETED("CLEANUP_COMPLETED", "自动清理完成"),
    PAYMENT_RESULT("PAYMENT_RESULT", "支付结果");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    NotificationTypeEnum(String value, String name) {
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
