package com.app.enums;

/**
 * 支付订单状态
 *
 * @author yanlei
 * @since 2026-09-09
 */
public enum PaymentOrderStatusEnum {
    PENDING("PENDING", "待支付"),
    PAID("PAID", "已支付"),
    CLOSED("CLOSED", "已关闭"),
    FAILED("FAILED", "支付失败"),
    REFUNDED("REFUNDED", "已退款");
    private final String value;

    /** 枚举中文名称 */
    private final String name;

    PaymentOrderStatusEnum(String value, String name) {
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
