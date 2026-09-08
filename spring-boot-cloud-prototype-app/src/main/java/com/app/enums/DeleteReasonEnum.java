package com.app.enums;

/**
 * 删除原因
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum DeleteReasonEnum {
    USER_MANUAL("USER_MANUAL", "用户主动删除"),
    ENTITLEMENT_EXPIRED("ENTITLEMENT_EXPIRED", "存储权益到期自动清理");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    DeleteReasonEnum(String value, String name) {
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
