package com.app.enums;

/**
 * 设备绑定状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum BindingStatusEnum {
    BOUND("BOUND", "已永久绑定");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    BindingStatusEnum(String value, String name) {
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
