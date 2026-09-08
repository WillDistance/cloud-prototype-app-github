package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_storage_plan表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_storage_plan")
public class StoragePlanEntity {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
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

    /** 增加容量(字节) */
    @TableField("capacity_bytes")
    private Long capacityBytes;

    /** 有效期数值 */
    @TableField("duration_value")
    private Integer durationValue;

    /** 有效期单位：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("duration_unit")
    private String durationUnit;

    /** 销售金额(美元、人民币) */
    @TableField("price_cent")
    private Long priceCent;

    /** 币种：USD=美元，CNY=人民币 */
    @TableField("currency")
    private String currency;

    /** 展示排序，数值越小越靠前 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否推荐：0=否，1=是 */
    @TableField("recommended")
    private Integer recommended;

    /** 状态，归档的数据类似于软删除：ACTIVE=生效销售中，OFF_SHELF=已下架，ARCHIVED=已归档 */
    @TableField("status")
    private String status;

    /** 生效时间(UTC，带毫秒) */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /** 创建人 */
    @TableField("create_by")
    private String createBy;

    /** 创建时间(UTC，带毫秒) */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField("update_by")
    private String updateBy;

    /** 更新时间(UTC，带毫秒) */
    @TableField("update_time")
    private LocalDateTime updateTime;

}
