package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.*;

/**
 * 用户多语言通知发送记录表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_notification")
public class NotificationEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 通知业务编号 */
    @TableField("notification_no")
    private String notificationNo;

    /** 通知用户ID */
    @TableField("user_id")
    private Long userId;

    /** 通知类型：VERIFY_CODE=邮箱验证码，EXPIRY_REMINDER=权益到期提醒，CLEANUP_STARTED=自动清理开始，CLEANUP_COMPLETED=自动清理完成，PAYMENT_RESULT=支付结果 */
    @TableField("notification_type")
    private NotificationTypeEnum notificationType;

    /** 通知语言：zh-CN=简体中文，en=英语，de=德语 */
    @TableField("language_code")
    private UserLanguageEnum languageCode;

    /** 发送渠道：EMAIL=电子邮件，IN_APP=应用内通知 */
    @TableField("channel")
    private NotificationChannelEnum channel;

    /** 接收地址或接收人标识 */
    @TableField("recipient")
    private String recipient;

    /** 通知标题快照 */
    @TableField("subject")
    private String subject;

    /** 通知正文快照 */
    @TableField("content")
    private String content;

    /** 关联业务类型 */
    @TableField("business_type")
    private String businessType;

    /** 关联业务主键ID */
    @TableField("business_id")
    private Long businessId;

    /** 通知幂等键 */
    @TableField("idempotency_key")
    private String idempotencyKey;

    /** 发送状态：PENDING=待发送，SENDING=发送中，SENT=发送成功，FAILED=发送失败，CANCELLED=已取消 */
    @TableField("status")
    private NotificationStatusEnum status;

    /** 已重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 下次重试时间（UTC，带毫秒） */
    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    /** 发送成功时间（UTC，带毫秒） */
    @TableField("sent_time")
    private LocalDateTime sentTime;

    /** 发送失败原因 */
    @TableField("failure_reason")
    private String failureReason;
}
