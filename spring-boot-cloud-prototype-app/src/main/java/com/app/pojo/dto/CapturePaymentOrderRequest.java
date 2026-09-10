package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 执行支付扣款请求。
 */
@Data
public class CapturePaymentOrderRequest {
    /** 商户订单号。 */
    @NotBlank
    @Size(max = 64)
    private String orderNo;
}
