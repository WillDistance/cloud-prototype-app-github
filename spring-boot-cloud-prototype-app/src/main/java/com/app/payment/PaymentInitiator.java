package com.app.payment;

import com.app.pojo.entity.PaymentOrderEntity;

/**
 * 支付发起器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface PaymentInitiator {
    /**
     * 发起支付
     *
     * @param order 支付订单
     * @return 支付发起结果
     */
    PaymentInitiation initiate(PaymentOrderEntity order);
}
