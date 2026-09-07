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
 * 用户独立存储权益表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_storage_entitlement")
public class StorageEntitlementEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 权益业务编号 */
    @TableField("entitlement_no")
    private String entitlementNo;

    /** 权益所属用户ID */
    @TableField("user_id")
    private Long userId;

    /** 权益来源：DEVICE_GIFT=设备绑定赠送，PURCHASE=用户购买 */
    @TableField("source_type")
    private EntitlementSourceTypeEnum sourceType;

    /** 设备赠送权益关联绑定ID */
    @TableField("device_binding_id")
    private Long deviceBindingId;

    /** 购买权益关联支付订单ID */
    @TableField("payment_order_id")
    private Long paymentOrderId;

    /** 权益名称快照 */
    @TableField("name_snapshot")
    private String nameSnapshot;

    /** 权益提供容量（字节） */
    @TableField("capacity_bytes")
    private Long capacityBytes;

    /** 权益有效期数值快照 */
    @TableField("duration_value")
    private Integer durationValue;

    /** 权益有效期单位：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("duration_unit")
    private DurationUnitEnum durationUnit;

    /** 计算自然月或自然年到期时间时使用的IANA时区快照 */
    @TableField("user_time_zone_snapshot")
    private String userTimeZoneSnapshot;

    /** 权益生效时间（UTC，带毫秒） */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /** 权益到期时间（UTC，带毫秒） */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 权益状态：ACTIVE=有效，EXPIRED=已到期，REVOKED=已撤销 */
    @TableField("status")
    private EntitlementStatusEnum status;

    /** 到期容量重算及清理处理完成时间（UTC，带毫秒） */
    @TableField("expired_processed_time")
    private LocalDateTime expiredProcessedTime;
}
