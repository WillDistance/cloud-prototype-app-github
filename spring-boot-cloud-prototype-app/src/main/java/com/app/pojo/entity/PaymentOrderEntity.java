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
 * 存储套餐支付订单表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_payment_order")
public class PaymentOrderEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单业务编号 */
    @TableField("order_no")
    private String orderNo;

    /** 下单用户ID */
    @TableField("user_id")
    private Long userId;

    /** 购买的存储套餐ID */
    @TableField("storage_plan_id")
    private Long storagePlanId;

    /** 套餐编码快照 */
    @TableField("plan_code_snapshot")
    private String planCodeSnapshot;

    /** 套餐版本快照 */
    @TableField("plan_version_snapshot")
    private Integer planVersionSnapshot;

    /** 套餐名称快照 */
    @TableField("plan_name_snapshot")
    private String planNameSnapshot;

    /** 套餐容量快照（字节） */
    @TableField("capacity_bytes_snapshot")
    private Long capacityBytesSnapshot;

    /** 套餐有效期数值快照 */
    @TableField("duration_value_snapshot")
    private Integer durationValueSnapshot;

    /** 套餐有效期单位快照：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("duration_unit_snapshot")
    private DurationUnitEnum durationUnitSnapshot;

    /** 订单金额（分） */
    @TableField("amount_cent")
    private Long amountCent;

    /** 货币类型 */
    @TableField("currency")
    private CurrencyEnum currency;

    /** 支付渠道 */
    @TableField("payment_channel")
    private PaymentChannelEnum paymentChannel;

    /** 订单状态：PENDING=待支付，PAID=已支付，CLOSED=已关闭，PAYMENT_FAILED=支付失败 */
    @TableField("status")
    private PaymentOrderStatusEnum status;

    /** 支付过期时间（UTC，带毫秒） */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 支付完成时间（UTC，带毫秒） */
    @TableField("paid_time")
    private LocalDateTime paidTime;

    /** 订单关闭时间（UTC，带毫秒） */
    @TableField("closed_time")
    private LocalDateTime closedTime;

    /** 客户端幂等请求号 */
    @TableField("client_request_id")
    private String clientRequestId;
}
