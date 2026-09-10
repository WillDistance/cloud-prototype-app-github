package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * t_photo_file表实体
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
@Accessors(chain = true)
@TableName("t_photo_file")
public class PhotoFileEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 照片所属用户ID，从上传会话的设备绑定的账号关系获取 */
    @TableField("user_id")
    private Long userId;

    /** 绑定设备ID */
    @TableField("device_id")
    private Long deviceId;

    /** 原始文件名 */
    @TableField("file_name")
    private String fileName;

    /** 原始文件扩展名 */
    @TableField("extension")
    private String extension;

    /** 原图MIME类型 */
    @TableField("mime_type")
    private String mimeType;

    /** 文件实际大小(字节)，用于容量计费 */
    @TableField("size_bytes")
    private Long sizeBytes;

    /** 上传时用户IANA时区快照 */
    @TableField("user_time_zone_snapshot")
    private String userTimeZoneSnapshot;

    /** OSS临时上传链接过期时间(UTC，带毫秒) */
    @TableField("upload_url_expire_time")
    private LocalDateTime uploadUrlExpireTime;

    /** 文件版本，当前仅保存原图记录。 */
    @TableField("file_type")
    private String fileType;

    /** OSS对象路径，不直接作为公网下载地址 */
    @TableField("object_key")
    private String objectKey;

    /**
     * 照片状态：照片状态：URL_ISSUED=已签发上传链接，ORIGINAL_UPLOADED=原图上传回调成功，PROCESSING=派生图处理中，AVAILABLE=可访问，FAILED=派生图处理失败，DELETE_PENDING=待永久删除，DELETE_FAILED=删除失败
     */
    @TableField("status")
    private String status;

    /** 删除原因：未删除时为空；USER_MANUAL=用户主动删除，ENTITLEMENT_EXPIRED=权益到期自动清理 */
    @TableField("delete_reason")
    private String deleteReason;

    /** 原图上传回调成功时间(UTC，带毫秒)，相册排序及清理依据 */
    @TableField("uploaded_time")
    private LocalDateTime uploadedTime;




}
