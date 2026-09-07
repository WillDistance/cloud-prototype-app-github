package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付订单响应
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class PaymentOrderVo {
    private String orderNo;
    private String planCodeSnapshot;
    private Integer planVersionSnapshot;
    private String planNameSnapshot;
    private Long capacityBytesSnapshot;
    private Integer durationValueSnapshot;
    private String durationUnitSnapshot;
    private Long amountCent;
    private String currency;
    private String paymentChannel;
    private String status;
    private LocalDateTime expireTime;
    private LocalDateTime paidTime;
    private LocalDateTime closedTime;
    private String clientRequestId;
    private String paymentRedirectUrl;
    private Map<String, String> paymentParameters;
}
