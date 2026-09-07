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
public enum DeleteReasonEnum {
    USER_MANUAL("USER_MANUAL"),
    ENTITLEMENT_EXPIRED("ENTITLEMENT_EXPIRED");

    @EnumValue
    private final String value;

    DeleteReasonEnum(String value) {
        this.value = value;
    }
}
