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
public enum EntitlementStatusEnum {
    ACTIVE("ACTIVE"),
    EXPIRED("EXPIRED"),
    REVOKED("REVOKED");

    @EnumValue
    private final String value;

    EntitlementStatusEnum(String value) {
        this.value = value;
    }
}
