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
public enum UploadRequestSourceEnum {
    BOUND_DEVICE("BOUND_DEVICE");

    @EnumValue
    private final String value;

    UploadRequestSourceEnum(String value) {
        this.value = value;
    }
}
