package com.app.enums;

/**
 * 平台配置状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PlatformConfigStatusEnum {
    ACTIVE("ACTIVE", "生效"),
    ARCHIVED("ARCHIVED", "已归档");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    PlatformConfigStatusEnum(String value, String name) {
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
