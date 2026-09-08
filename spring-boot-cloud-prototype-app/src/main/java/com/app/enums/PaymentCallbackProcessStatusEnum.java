package com.app.enums;

/**
 * 支付回调处理状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PaymentCallbackProcessStatusEnum {
    RECEIVED("RECEIVED", "已接收"),
    SUCCESS("SUCCESS", "处理成功"),
    REJECTED("REJECTED", "已拒绝"),
    FAILED("FAILED", "处理失败"),
    DUPLICATE("DUPLICATE", "重复回调");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    PaymentCallbackProcessStatusEnum(String value, String name) {
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
