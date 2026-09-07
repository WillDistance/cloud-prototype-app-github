package com.app.payment;

import java.util.Map;

/**
 * 支付发起结果
 *
 * @param redirectUrl 支付跳转链接
 * @param parameters  支付参数
 * @author yanlei
 * @since 2026-09-06
 */
public record PaymentInitiation(String redirectUrl, Map<String, String> parameters) {
}
