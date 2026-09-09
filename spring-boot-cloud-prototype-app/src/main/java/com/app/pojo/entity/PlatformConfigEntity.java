package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_platform_config表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_platform_config")
public class PlatformConfigEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 配置版本号 */
    @TableField("config_version")
    private String configVersion;

    /** 单文件大小上限(字节) */
    @TableField("max_file_size_bytes")
    private Long maxFileSizeBytes;

    /** 允许的文件扩展名JSON数组，例如["jpg","jpeg","png","heic"] */
    @TableField("allowed_extensions")
    private String allowedExtensions;

    /** 设备绑定赠送容量(字节) */
    @TableField("device_gift_capacity_bytes")
    private Long deviceGiftCapacityBytes;

    /** 设备赠送有效期数值 */
    @TableField("device_gift_duration_value")
    private Integer deviceGiftDurationValue;

    /** 设备赠送有效期单位：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("device_gift_duration_unit")
    private String deviceGiftDurationUnit;

    /** 状态，仅能存在一条生效中的数据，归档的数据类似于软删除：ACTIVE=生效，ARCHIVED=已归档 */
    @TableField("status")
    private String status;




}
