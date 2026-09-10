package com.app.enums;

/**
 * 支付渠道
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PaymentChannelEnum {
    PAYPAL("PAYPAL", "PayPal Checkout"),
    PINGPONG("PINGPONG", "乒乓支付");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    PaymentChannelEnum(String value, String name) {
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
