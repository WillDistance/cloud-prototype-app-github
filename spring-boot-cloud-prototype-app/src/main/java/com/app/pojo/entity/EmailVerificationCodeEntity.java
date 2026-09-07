package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.VerificationPurposeEnum;
import com.app.enums.VerificationStatusEnum;

/**
 * 邮箱验证码表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_email_verification_code")
public class EmailVerificationCodeEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 接收验证码的邮箱 */
    @TableField("email")
    private String email;

    /** 关联用户ID，注册场景可为空 */
    @TableField("user_id")
    private Long userId;

    /** 用途：REGISTER=注册，RESET_PASSWORD=重置密码 */
    @TableField("purpose")
    private VerificationPurposeEnum purpose;

    /** 验证码安全哈希，禁止保存明文 */
    @TableField("code_hash")
    private String codeHash;

    /** 状态：PENDING=待验证，VERIFIED=已验证，EXPIRED=已过期，INVALIDATED=已作废 */
    @TableField("status")
    private VerificationStatusEnum status;

    /** 发送次数 */
    @TableField("send_count")
    private Integer sendCount;

    /** 验证失败次数 */
    @TableField("verify_fail_count")
    private Integer verifyFailCount;

    /** 过期时间（UTC，带毫秒） */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 验证通过时间（UTC，带毫秒） */
    @TableField("verified_time")
    private LocalDateTime verifiedTime;

    /** 请求来源IP */
    @TableField("request_ip")
    private String requestIp;
}
