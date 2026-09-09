package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_user表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_user")
public class UserEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录邮箱，统一规范化为小写 */
    @TableField("email")
    private String email;

    /** 登录密码安全哈希，禁止保存明文，使用ARGON2ID算法 */
    @TableField("password_hash")
    private String passwordHash;

    /** IANA时区标识，例如Asia/Shanghai */
    @TableField("time_zone")
    private String timeZone;

    /** 服务端通知语言偏好：zh-CN=简体中文，en=英语，de=德语 */
    @TableField("preferred_language")
    private String preferredLanguage;

    /** 账号状态：ACTIVE=正常可用，LOCKED=已锁定，DISABLED=已禁用 */
    @TableField("status")
    private String status;

    /** 最近登录时间(UTC，带毫秒) */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 密码最近更新时间(UTC，带毫秒) */
    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;




}
