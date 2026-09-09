package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_email_verification_code表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_email_verification_code")
public class EmailVerificationCodeEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 接收验证码的邮箱 */
    @TableField("email")
    private String email;

    /** 关联用户ID，注册场景可为空 */
    @TableField("user_id")
    private Long userId;

    /** 用途：REGISTER=注册，RESET_PASSWORD=重置密码 */
    @TableField("purpose")
    private String purpose;

    /** 验证码明文保存 */
    @TableField("code")
    private String code;

    /** 状态：SEND_FAIL=发送失败，PENDING=待验证，VERIFIED=已验证，EXPIRED=已过期，INVALIDATED=已作废 */
    @TableField("status")
    private String status;

    /** 发送次数 */
    @TableField("send_count")
    private Integer sendCount;

    /** 验证失败次数 */
    @TableField("verify_fail_count")
    private Integer verifyFailCount;

    /** 验证码过期时间(UTC，带毫秒) */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 验证成功时间(UTC，带毫秒) */
    @TableField("verified_time")
    private LocalDateTime verifiedTime;

    /** 申请验证码的客户端IP */
    @TableField("request_ip")
    private String requestIp;




}
