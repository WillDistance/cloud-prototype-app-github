package com.app.enums;

/**
 * 存储套餐状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum StoragePlanStatusEnum {
    ACTIVE("ACTIVE", "生效销售中"),
    OFF_SHELF("OFF_SHELF", "已下架"),
    ARCHIVED("ARCHIVED", "已归档");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    StoragePlanStatusEnum(String value, String name) {
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
