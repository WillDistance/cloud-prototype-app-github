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
public enum StoragePlanStatusEnum {
    ACTIVE("ACTIVE"),
    OFF_SHELF("OFF_SHELF"),
    ARCHIVED("ARCHIVED");

    @EnumValue
    private final String value;

    StoragePlanStatusEnum(String value) {
        this.value = value;
    }
}
