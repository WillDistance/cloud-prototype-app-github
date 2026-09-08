package com.app.enums;

/**
 * 验证码状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum VerificationStatusEnum {
    SEND_FAIL("SEND_FAIL", "发送失败"),
    PENDING("PENDING", "待验证"),
    VERIFIED("VERIFIED", "已验证"),
    EXPIRED("EXPIRED", "已过期"),
    INVALIDATED("INVALIDATED", "已作废");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    VerificationStatusEnum(String value, String name) {
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
