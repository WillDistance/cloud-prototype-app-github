package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建支付订单请求。
 */
@Data
public class CreatePaymentOrderRequest {
    /** 套餐编码。 */
    @NotBlank
    @Size(max = 64)
    private String planCode;

    /** 套餐版本。 */
    private Integer planVersion;

    /** 客户端幂等号，重试同一个请求不会创建重复订单。 */
    @NotBlank
    @Size(max = 64)
    private String clientRequestId;
}
