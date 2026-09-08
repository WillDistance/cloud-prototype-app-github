package com.app.enums;

/**
 * 权益状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum EntitlementStatusEnum {
    ACTIVE("ACTIVE", "有效"),
    EXPIRED("EXPIRED", "已到期"),
    REVOKED("REVOKED", "已撤销");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    EntitlementStatusEnum(String value, String name) {
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
