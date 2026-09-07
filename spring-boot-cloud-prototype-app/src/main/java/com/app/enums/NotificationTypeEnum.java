package com.app.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 数据库存储枚举
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
public enum NotificationTypeEnum {
    VERIFY_CODE("VERIFY_CODE"),
    EXPIRY_REMINDER("EXPIRY_REMINDER"),
    CLEANUP_STARTED("CLEANUP_STARTED"),
    CLEANUP_COMPLETED("CLEANUP_COMPLETED"),
    PAYMENT_RESULT("PAYMENT_RESULT");

    @EnumValue
    private final String value;

    NotificationTypeEnum(String value) {
        this.value = value;
    }
}
