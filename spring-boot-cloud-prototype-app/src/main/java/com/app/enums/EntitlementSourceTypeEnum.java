package com.app.enums;

/**
 * 权益来源
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum EntitlementSourceTypeEnum {
    DEVICE_GIFT("DEVICE_GIFT", "设备绑定赠送"),
    PURCHASE("PURCHASE", "用户购买");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    EntitlementSourceTypeEnum(String value, String name) {
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
