package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_payment_callback表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_payment_callback")
public class PaymentCallbackEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联订单ID，无法匹配订单时可为空 */
    @TableField("payment_order_id")
    private Long paymentOrderId;

    /** 回调中的商户订单号 */
    @TableField("order_no")
    private String orderNo;

    /** 支付渠道：PAYPAL=PayPal PINGPONG=乒乓支付 */
    @TableField("payment_channel")
    private String paymentChannel;

    /** 支付平台交易号 */
    @TableField("channel_transaction_no")
    private String channelTransactionNo;

    /** 支付平台回调事件ID */
    @TableField("callback_event_id")
    private String callbackEventId;

    /** 支付回调原文 */
    @TableField("raw_payload")
    private String rawPayload;

    /** 回调签名原文 */
    @TableField("signature_value")
    private String signatureValue;

    /** 验签结果：0=失败，1=成功 */
    @TableField("signature_verified")
    private Integer signatureVerified;

    /** 回调支付金额(分) */
    @TableField("callback_amount_cent")
    private Long callbackAmountCent;

    /** 回调币种 */
    @TableField("callback_currency")
    private String callbackCurrency;

    /** 支付平台回调状态原值 */
    @TableField("callback_status")
    private String callbackStatus;

    /** 处理状态：RECEIVED=已接收，SUCCESS=处理成功，REJECTED=已拒绝，FAILED=处理失败，DUPLICATE=重复回调 */
    @TableField("process_status")
    private String processStatus;

    /** 验签或处理失败原因 */
    @TableField("failure_reason")
    private String failureReason;

    /** 回调接收时间(UTC，带毫秒) */
    @TableField("received_time")
    private LocalDateTime receivedTime;

    /** 回调处理完成时间(UTC，带毫秒) */
    @TableField("processed_time")
    private LocalDateTime processedTime;




}
