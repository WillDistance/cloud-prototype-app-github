package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.DurationUnitEnum;
import com.app.enums.PlatformConfigStatusEnum;

/**
 * 平台全局配置表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName(value = "t_platform_config", autoResultMap = true)
public class PlatformConfigEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 配置版本号 */
    @TableField("config_version")
    private String configVersion;

    /** 单文件大小上限（字节） */
    @TableField("max_file_size_bytes")
    private Long maxFileSizeBytes;

    /** 允许的文件扩展名列表 */
    @TableField(value = "allowed_extensions", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private java.util.List<String> allowedExtensions;

    /** 允许的MIME类型列表 */
    @TableField(value = "allowed_mime_types", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private java.util.List<String> allowedMimeTypes;

    /** 设备赠送容量（字节） */
    @TableField("device_gift_capacity_bytes")
    private Long deviceGiftCapacityBytes;

    /** 设备赠送有效期数值 */
    @TableField("device_gift_duration_value")
    private Integer deviceGiftDurationValue;

    /** 设备赠送有效期单位：DAY=天，MONTH=自然月，YEAR=自然年 */
    @TableField("device_gift_duration_unit")
    private DurationUnitEnum deviceGiftDurationUnit;

    /** 状态：DRAFT=草稿，ACTIVE=生效，INACTIVE=未生效，ARCHIVED=已归档 */
    @TableField("status")
    private PlatformConfigStatusEnum status;

    /** 生效时间（UTC，带毫秒） */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
}
