package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.CurrencyEnum;
import com.app.enums.DurationUnitEnum;
import com.app.enums.StoragePlanStatusEnum;

/**
 * 云存储套餐表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_storage_plan")
public class StoragePlanEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 套餐编码 */
    @TableField("plan_code")
    private String planCode;

    /** 套餐版本 */
    @TableField("plan_version")
    private Integer planVersion;

    /** 套餐名称 */
    @TableField("plan_name")
    private String planName;

    /** 套餐提供容量（字节） */
    @TableField("capacity_bytes")
    private Long capacityBytes;

    /** 套餐有效期数值 */
    @TableField("duration_value")
    private Integer durationValue;

    /** 套餐有效期单位：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("duration_unit")
    private DurationUnitEnum durationUnit;

    /** 套餐价格（分） */
    @TableField("price_cent")
    private Long priceCent;

    /** 货币类型 */
    @TableField("currency")
    private CurrencyEnum currency;

    /** 展示排序，数值越小越靠前 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否推荐：0=否，1=是 */
    @TableField("recommended")
    private Boolean recommended;

    /** 状态：ACTIVE=在售，INACTIVE=下架，ARCHIVED=已归档 */
    @TableField("status")
    private StoragePlanStatusEnum status;

    /** 生效时间（UTC，带毫秒） */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
}
