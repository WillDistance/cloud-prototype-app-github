package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 支付订单响应。
 */
@Getter
@AllArgsConstructor
public class PaymentOrderVo {
    private String orderNo;
    private String status;
    private String paymentChannel;
    private String paymentMethod;
    private Long amountCent;
    private String currency;
    private String providerOrderId;
    private String checkoutUrl;
    private LocalDateTime expireTime;
    private LocalDateTime paidTime;
}
