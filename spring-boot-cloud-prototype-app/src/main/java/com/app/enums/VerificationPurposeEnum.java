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
public enum VerificationPurposeEnum {
    REGISTER("REGISTER"),
    RESET_PASSWORD("RESET_PASSWORD");

    @EnumValue
    private final String value;

    VerificationPurposeEnum(String value) {
        this.value = value;
    }
}
