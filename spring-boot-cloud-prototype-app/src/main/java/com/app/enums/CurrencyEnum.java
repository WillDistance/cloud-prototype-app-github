package com.app.enums;

/**
 * 币种
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum CurrencyEnum {
    CNY("CNY", "人民币"),
    USD("USD", "美元");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    CurrencyEnum(String value, String name) {
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
