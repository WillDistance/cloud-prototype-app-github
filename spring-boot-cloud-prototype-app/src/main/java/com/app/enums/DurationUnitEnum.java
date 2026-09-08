package com.app.enums;

/**
 * 有效期单位
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum DurationUnitEnum {
    DAY("DAY", "天"),
    MONTH("MONTH", "自然月"),
    YEAR("YEAR", "自然年");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    DurationUnitEnum(String value, String name) {
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
