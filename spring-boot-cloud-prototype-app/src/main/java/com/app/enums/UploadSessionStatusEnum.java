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
public enum UploadSessionStatusEnum {
    URL_ISSUED("URL_ISSUED"),
    ORIGINAL_UPLOADED("ORIGINAL_UPLOADED"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    EXPIRED("EXPIRED");

    @EnumValue
    private final String value;

    UploadSessionStatusEnum(String value) {
        this.value = value;
    }
}
