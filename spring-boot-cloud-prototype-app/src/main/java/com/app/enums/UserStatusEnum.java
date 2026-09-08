package com.app.enums;

/**
 * 账号状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum UserStatusEnum {
    ACTIVE("ACTIVE", "正常可用"),
    LOCKED("LOCKED", "已锁定"),
    DISABLED("DISABLED", "已禁用");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    UserStatusEnum(String value, String name) {
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
