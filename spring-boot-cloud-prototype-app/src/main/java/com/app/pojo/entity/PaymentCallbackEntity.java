package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.*;

/**
 * 乒乓支付异步回调与审计表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_payment_callback")
public class PaymentCallbackEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联支付订单ID */
    @TableField("payment_order_id")
    private Long paymentOrderId;

    /** 订单业务编号 */
    @TableField("order_no")
    private String orderNo;

    /** 支付渠道 */
    @TableField("payment_channel")
    private PaymentChannelEnum paymentChannel;

    /** 支付渠道交易号 */
    @TableField("channel_transaction_no")
    private String channelTransactionNo;

    /** 支付渠道回调通知唯一ID */
    @TableField("callback_event_id")
    private String callbackEventId;

    /** 回调原文哈希 */
    @TableField("callback_content_hash")
    private String callbackContentHash;

    /** 回调原始报文 */
    @TableField("raw_payload")
    private String rawPayload;

    /** 回调签名值 */
    @TableField("signature_value")
    private String signatureValue;

    /** 是否验签通过：0=否，1=是 */
    @TableField("signature_verified")
    private Boolean signatureVerified;

    /** 回调金额（分） */
    @TableField("callback_amount_cent")
    private Long callbackAmountCent;

    /** 回调货币类型 */
    @TableField("callback_currency")
    private CurrencyEnum callbackCurrency;

    /** 支付渠道回调状态 */
    @TableField("callback_status")
    private String callbackStatus;

    /** 回调处理状态：RECEIVED=已接收，SUCCESS=成功，REJECTED=已拒绝，FAILED=失败，DUPLICATE=重复 */
    @TableField("process_status")
    private CallbackProcessStatusEnum processStatus;

    /** 处理失败原因 */
    @TableField("failure_reason")
    private String failureReason;

    /** 回调接收时间（UTC，带毫秒） */
    @TableField("received_time")
    private LocalDateTime receivedTime;

    /** 回调处理时间（UTC，带毫秒） */
    @TableField("processed_time")
    private LocalDateTime processedTime;
}
