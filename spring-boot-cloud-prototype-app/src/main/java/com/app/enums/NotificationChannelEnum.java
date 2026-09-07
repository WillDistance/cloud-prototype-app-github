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
public enum NotificationChannelEnum {
    EMAIL("EMAIL"),
    IN_APP("IN_APP");

    @EnumValue
    private final String value;

    NotificationChannelEnum(String value) {
        this.value = value;
    }
}
