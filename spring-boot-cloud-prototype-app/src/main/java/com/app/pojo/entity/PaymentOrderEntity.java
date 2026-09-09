package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_payment_order表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_payment_order")
public class PaymentOrderEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 商户订单号 */
    @TableField("order_no")
    private String orderNo;

    /** 下单用户ID */
    @TableField("user_id")
    private Long userId;

    /** 下单时选择的套餐ID */
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

    /** 套餐容量快照(字节) */
    @TableField("capacity_bytes_snapshot")
    private Long capacityBytesSnapshot;

    /** 套餐有效期数值快照 */
    @TableField("duration_value_snapshot")
    private Integer durationValueSnapshot;

    /** 套餐有效期单位快照：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("duration_unit_snapshot")
    private String durationUnitSnapshot;

    /** 应付金额(美元、人民币) */
    @TableField("amount_cent")
    private Long amountCent;

    /** 币种：USD=美元，CNY=人民币 */
    @TableField("currency")
    private String currency;

    /** 支付渠道：PINGPONG=乒乓支付 */
    @TableField("payment_channel")
    private String paymentChannel;

    /** 订单状态：PENDING=待支付，PAID=已支付，CLOSED=已关闭，FAILED=支付失败，REFUNDED=已退款 */
    @TableField("status")
    private String status;

    /** 订单支付过期时间(UTC，带毫秒) */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 支付成功时间(UTC，带毫秒) */
    @TableField("paid_time")
    private LocalDateTime paidTime;

    /** 订单关闭时间(UTC，带毫秒) */
    @TableField("closed_time")
    private LocalDateTime closedTime;

    /** 客户端创建订单幂等号 */
    @TableField("client_request_id")
    private String clientRequestId;




}
