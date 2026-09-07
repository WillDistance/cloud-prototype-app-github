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
public enum EntitlementSourceTypeEnum {
    DEVICE_GIFT("DEVICE_GIFT"),
    PURCHASE("PURCHASE");

    @EnumValue
    private final String value;

    EntitlementSourceTypeEnum(String value) {
        this.value = value;
    }
}
