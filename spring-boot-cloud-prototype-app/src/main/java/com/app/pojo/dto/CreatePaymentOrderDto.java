package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 创建支付订单请求
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class CreatePaymentOrderDto {
    @NotBlank(message = "套餐编码不能为空")
    private String planCode;

    @NotNull(message = "套餐版本不能为空")
    private Integer planVersion;

    @NotBlank(message = "客户端幂等号不能为空")
    private String clientRequestId;
}
