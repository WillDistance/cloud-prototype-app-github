package com.app.service;

import com.app.pojo.dto.CreatePaymentOrderDto;
import com.app.pojo.vo.PaymentOrderVo;

/**
 * 支付订单服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface PaymentOrderService {
    /**
     * 根据请求参数创建或返回幂等支付订单。
     *
     * @param dto 支付订单创建请求
     * @return 支付订单信息
     */
    PaymentOrderVo createOrder(CreatePaymentOrderDto dto);

    /**
     * 查询当前用户的支付订单。
     *
     * @param orderNo 支付订单号
     * @return 支付订单信息
     */
    PaymentOrderVo getOrder(String orderNo);
}
