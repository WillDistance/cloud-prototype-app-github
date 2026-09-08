package com.app.enums;

/**
 * 验证码用途
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum VerificationPurposeEnum {
    REGISTER("REGISTER", "注册"),
    RESET_PASSWORD("RESET_PASSWORD", "重置密码");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    VerificationPurposeEnum(String value, String name) {
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
