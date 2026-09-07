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
public enum PaymentOrderStatusEnum {
    PENDING("PENDING"),
    PAID("PAID"),
    CLOSED("CLOSED"),
    FAILED("FAILED"),
    REFUNDED("REFUNDED");

    @EnumValue
    private final String value;

    PaymentOrderStatusEnum(String value) {
        this.value = value;
    }
}
