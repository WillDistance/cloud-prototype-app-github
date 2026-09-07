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
public enum NotificationStatusEnum {
    PENDING("PENDING"),
    SENDING("SENDING"),
    SENT("SENT"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    @EnumValue
    private final String value;

    NotificationStatusEnum(String value) {
        this.value = value;
    }
}
