package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户存储用量账户表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_user_storage_account")
public class UserStorageAccountEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 已确认照片占用容量（字节，仅计算原图） */
    @TableField("used_bytes")
    private Long usedBytes;

    /** 上传处理中预留容量（字节） */
    @TableField("reserved_bytes")
    private Long reservedBytes;

    /** 乐观锁版本号 */
    @TableField("lock_version")
    private Long lockVersion;
}
