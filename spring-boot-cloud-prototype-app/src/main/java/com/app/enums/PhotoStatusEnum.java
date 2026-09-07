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
public enum PhotoStatusEnum {
    AVAILABLE("AVAILABLE"),
    DELETE_PENDING("DELETE_PENDING"),
    DELETE_FAILED("DELETE_FAILED");

    @EnumValue
    private final String value;

    PhotoStatusEnum(String value) {
        this.value = value;
    }
}
