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
 * 设备OSS临时上传会话表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_upload_session")
public class UploadSessionEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 上传会话业务编号 */
    @TableField("upload_no")
    private String uploadNo;

    /** 上传请求来源：BOUND_DEVICE=用户已永久绑定的设备 */
    @TableField("request_source")
    private UploadRequestSourceEnum requestSource;

    /** 设备鉴权方式：DEVICE_PASSWORD=设备ID与设备密码鉴权 */
    @TableField("device_auth_type")
    private DeviceAuthTypeEnum deviceAuthType;

    /** 照片归属用户ID，由设备永久绑定关系确定 */
    @TableField("user_id")
    private Long userId;

    /** 通过鉴权的绑定设备表主键ID */
    @TableField("device_id")
    private Long deviceId;

    /** 申请设备对应的唯一永久绑定关系ID */
    @TableField("device_binding_id")
    private Long deviceBindingId;

    /** 设备身份及永久绑定关系校验通过时间（UTC，带毫秒） */
    @TableField("device_authenticated_time")
    private LocalDateTime deviceAuthenticatedTime;

    /** 上传时读取的用户IANA时区快照 */
    @TableField("user_time_zone_snapshot")
    private String userTimeZoneSnapshot;

    /** 设备上报的原文件名 */
    @TableField("original_file_name")
    private String originalFileName;

    /** 设备声明的MIME类型 */
    @TableField("declared_content_type")
    private String declaredContentType;

    /** 设备声明的文件大小（字节） */
    @TableField("declared_file_size_bytes")
    private Long declaredFileSizeBytes;

    /** 本次上传预留容量（字节） */
    @TableField("reserved_bytes")
    private Long reservedBytes;

    /** 服务端生成的OSS原图对象路径 */
    @TableField("original_object_key")
    private String originalObjectKey;

    /** OSS临时上传链接过期时间（UTC，带毫秒） */
    @TableField("upload_url_expire_time")
    private LocalDateTime uploadUrlExpireTime;

    /** 状态：URL_ISSUED=已签发，ORIGINAL_UPLOADED=原图已上传，PROCESSING=处理中，COMPLETED=完成，FAILED=失败，EXPIRED=已过期 */
    @TableField("status")
    private UploadSessionStatusEnum status;

    /** OSS回调事件ID，用于幂等 */
    @TableField("oss_callback_event_id")
    private String ossCallbackEventId;

    /** 失败码 */
    @TableField("failure_code")
    private String failureCode;

    /** 失败原因 */
    @TableField("failure_reason")
    private String failureReason;

    /** 上传处理完成时间（UTC，带毫秒） */
    @TableField("completed_time")
    private LocalDateTime completedTime;
}
