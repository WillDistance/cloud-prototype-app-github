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
public enum UserStatusEnum {
    ACTIVE("ACTIVE"),
    LOCKED("LOCKED"),
    DISABLED("DISABLED");

    @EnumValue
    private final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }
}
