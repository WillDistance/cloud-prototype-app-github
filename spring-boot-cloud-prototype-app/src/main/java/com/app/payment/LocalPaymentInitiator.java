package com.app.payment;

import com.app.pojo.entity.PaymentOrderEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 本地安全支付占位实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class LocalPaymentInitiator implements PaymentInitiator {
    private final String paymentBaseUrl;
    private final String merchantId;

    public LocalPaymentInitiator(
            @Value("${payment.pingpong.base-url:https://pay.invalid/checkout}") String paymentBaseUrl,
            @Value("${PINGPONG_MERCHANT_ID:local-placeholder}") String merchantId) {
        this.paymentBaseUrl = paymentBaseUrl;
        this.merchantId = merchantId;
    }

    @Override
    public PaymentInitiation initiate(PaymentOrderEntity order) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("merchantId", merchantId);
        parameters.put("orderNo", order.getOrderNo());
        parameters.put("amountCent", String.valueOf(order.getAmountCent()));
        parameters.put("currency", order.getCurrency().getValue());
        parameters.put("expireTime", order.getExpireTime().toString());
        return new PaymentInitiation(paymentBaseUrl + "?orderNo=" + order.getOrderNo(), parameters);
    }
}
