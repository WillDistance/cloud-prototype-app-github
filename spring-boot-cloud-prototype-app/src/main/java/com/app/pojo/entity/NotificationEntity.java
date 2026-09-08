package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * t_notification表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_notification")
public class NotificationEntity {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 通知业务编号 */
    @TableField("notification_no")
    private String notificationNo;

    /** 通知用户ID */
    @TableField("user_id")
    private Long userId;

    /** 通知类型：VERIFY_CODE=邮箱验证码，EXPIRY_REMINDER=权益到期提醒，CLEANUP_STARTED=自动清理开始，CLEANUP_COMPLETED=自动清理完成，PAYMENT_RESULT=支付结果 */
    @TableField("notification_type")
    private String notificationType;

    /** 通知语言：zh-CN=简体中文，en=英语，de=德语 */
    @TableField("language_code")
    private String languageCode;

    /** 发送渠道：EMAIL=电子邮件，IN_APP=应用内通知 */
    @TableField("channel")
    private String channel;

    /** 接收地址或接收人标识 */
    @TableField("recipient")
    private String recipient;

    /** 通知标题 */
    @TableField("subject")
    private String subject;

    /** 通知正文 */
    @TableField("content")
    private String content;

    /** 发送状态：PENDING=待发送，SENDING=发送中，SENT=发送成功，FAILED=发送失败 */
    @TableField("status")
    private String status;

    /** 已重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 下次重试时间(UTC，带毫秒) */
    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    /** 发送成功时间(UTC，带毫秒) */
    @TableField("sent_time")
    private LocalDateTime sentTime;

    /** 发送失败原因 */
    @TableField("failure_reason")
    private String failureReason;

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
