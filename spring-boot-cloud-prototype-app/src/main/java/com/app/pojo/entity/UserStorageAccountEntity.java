package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_user_storage_account表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_user_storage_account")
public class UserStorageAccountEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 已确认照片占用容量(字节，仅计算原图) */
    @TableField("used_bytes")
    private Long usedBytes;

    /** 上传处理中预留容量(字节) */
    @TableField("reserved_bytes")
    private Long reservedBytes;

    /** 乐观锁版本号 */
    @TableField("lock_version")
    private Long lockVersion;




}
