package com.app.enums;

/**
 * 服务端通知语言偏好
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum LanguageEnum {
    ZH_CN("zh-CN", "简体中文"),
    EN("en", "英语"),
    DE("de", "德语");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    LanguageEnum(String value, String name) {
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
