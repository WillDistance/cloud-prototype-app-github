package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_device表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_device")
public class DeviceEntity {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 设备业务ID，例如CC-2026-AB12-8A2F */
    @TableField("device_id")
    private String deviceId;

    /** 设备初始绑定密码安全哈希，使用ARGON2ID算法 */
    @TableField("initial_password_hash")
    private String initialPasswordHash;

    /** 设备型号 */
    @TableField("model")
    private String model;

    /** 设备状态：UNBOUND=未绑定，BOUND=已绑定，DISABLED=已禁用 */
    @TableField("status")
    private String status;

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
