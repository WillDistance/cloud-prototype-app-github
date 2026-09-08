package com.app.enums;

/**
 * 照片文件版本
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PhotoFileTypeEnum {
    ORIGINAL("ORIGINAL", "原图"),
    THUMBNAIL("THUMBNAIL", "缩略图"),
    PREVIEW("PREVIEW", "预览图");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    PhotoFileTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
