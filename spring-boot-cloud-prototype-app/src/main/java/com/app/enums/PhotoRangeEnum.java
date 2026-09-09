package com.app.enums;

/**
 * 相册时间筛选范围
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PhotoRangeEnum {
    TODAY("TODAY", "今天"),
    SEVEN_DAYS("SEVEN_DAYS", "最近7天"),
    ONE_MONTH("ONE_MONTH", "最近1个月"),
    ALL("ALL", "全部");

    private final String value;
    private final String name;

    PhotoRangeEnum(String value, String name) {
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
