package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.DeviceBindingStatusEnum;

/**
 * 用户设备永久绑定关系表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_device_binding")
public class DeviceBindingEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 绑定用户ID */
    @TableField("user_id")
    private Long userId;

    /** 设备表主键ID */
    @TableField("device_id")
    private Long deviceId;

    /** 永久绑定时间（UTC，带毫秒） */
    @TableField("bind_time")
    private LocalDateTime bindTime;

    /** 绑定时用户IANA时区快照 */
    @TableField("bind_user_time_zone")
    private String bindUserTimeZone;

    /** 绑定状态：BOUND=已永久绑定（当前仅允许该值） */
    @TableField("status")
    private DeviceBindingStatusEnum status;
}
