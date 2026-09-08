package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_device_binding表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_device_binding")
public class DeviceBindingEntity {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 绑定用户ID */
    @TableField("user_id")
    private Long userId;

    /** 设备表主键ID */
    @TableField("device_id")
    private Long deviceId;

    /** 永久绑定时间(UTC，带毫秒) */
    @TableField("bind_time")
    private LocalDateTime bindTime;

    /** 绑定时用户IANA时区快照 */
    @TableField("bind_user_time_zone")
    private String bindUserTimeZone;

    /** 绑定状态：BOUND=已永久绑定（当前仅允许该值） */
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
