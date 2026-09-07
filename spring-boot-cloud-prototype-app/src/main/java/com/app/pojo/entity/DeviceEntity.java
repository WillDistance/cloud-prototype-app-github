package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.DeviceStatusEnum;

/**
 * 云端相机设备表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_device")
public class DeviceEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 设备业务ID，例如CC-2026-AB12-8A2F */
    @TableField("device_id")
    private String deviceId;

    /** 设备初始绑定密码安全哈希 */
    @TableField("initial_password_hash")
    private String initialPasswordHash;

    /** 设备密码哈希算法 */
    @TableField("password_algorithm")
    private String passwordAlgorithm;

    /** 设备型号 */
    @TableField("model")
    private String model;

    /** 设备状态：UNBOUND=未绑定，BOUND=已绑定，DISABLED=已禁用 */
    @TableField("status")
    private DeviceStatusEnum status;

    /** 最近在线时间（UTC，带毫秒） */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;
}
