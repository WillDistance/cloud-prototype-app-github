package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.app.enums.PhotoStatusEnum;

/**
 * 可访问照片元数据表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_photo")
public class PhotoEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 照片业务编号 */
    @TableField("photo_no")
    private String photoNo;

    /** 来源设备上传会话ID */
    @TableField("upload_session_id")
    private Long uploadSessionId;

    /** 照片所属用户ID */
    @TableField("user_id")
    private Long userId;

    /** 实际上传设备表主键ID */
    @TableField("device_id")
    private Long deviceId;

    /** 上传时校验的永久绑定关系ID */
    @TableField("device_binding_id")
    private Long deviceBindingId;

    /** 原始文件名 */
    @TableField("file_name")
    private String fileName;

    /** 原始文件扩展名 */
    @TableField("original_extension")
    private String originalExtension;

    /** 原图MIME类型 */
    @TableField("original_mime_type")
    private String originalMimeType;

    /** 原图实际大小（字节），用于容量计费 */
    @TableField("original_size_bytes")
    private Long originalSizeBytes;

    /** 原图SHA-256，仅用于审计和排障 */
    @TableField("original_sha256")
    private String originalSha256;

    /** 原图宽度（像素） */
    @TableField("width_pixels")
    private Integer widthPixels;

    /** 原图高度（像素） */
    @TableField("height_pixels")
    private Integer heightPixels;

    /** 照片拍摄时间（UTC，带毫秒），可为空 */
    @TableField("taken_time")
    private LocalDateTime takenTime;

    /** 原图上传完成时间（UTC，带毫秒） */
    @TableField("uploaded_time")
    private LocalDateTime uploadedTime;

    /** 上传时用户IANA时区快照 */
    @TableField("user_time_zone_snapshot")
    private String userTimeZoneSnapshot;

    /** 照片状态：AVAILABLE=可访问，DELETE_PENDING=待永久删除，DELETE_FAILED=删除失败 */
    @TableField("status")
    private PhotoStatusEnum status;
}
