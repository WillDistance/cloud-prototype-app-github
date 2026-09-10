package com.app.service;

import com.app.pojo.dto.CapturePaymentOrderRequest;
import com.app.pojo.dto.CreatePaymentOrderRequest;
import com.app.pojo.vo.PaymentOrderVo;

import java.util.Map;

/**
 * 支付订单业务接口。
 */
public interface PaymentOrderService {
    PaymentOrderVo createOrder(CreatePaymentOrderRequest request);

    PaymentOrderVo captureOrder(CapturePaymentOrderRequest request);

    PaymentOrderVo getOrder(String orderNo);

    void handlePayPalWebhook(String rawPayload, Map<String, String> headers);
}
