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
public enum DeviceAuthTypeEnum {
    DEVICE_PASSWORD("DEVICE_PASSWORD");

    @EnumValue
    private final String value;

    DeviceAuthTypeEnum(String value) {
        this.value = value;
    }
}
