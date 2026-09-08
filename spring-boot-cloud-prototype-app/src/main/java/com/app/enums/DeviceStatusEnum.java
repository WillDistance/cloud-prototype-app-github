package com.app.enums;

/**
 * 设备状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum DeviceStatusEnum {
    UNBOUND("UNBOUND", "未绑定"),
    BOUND("BOUND", "已绑定"),
    DISABLED("DISABLED", "已禁用");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    DeviceStatusEnum(String value, String name) {
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
