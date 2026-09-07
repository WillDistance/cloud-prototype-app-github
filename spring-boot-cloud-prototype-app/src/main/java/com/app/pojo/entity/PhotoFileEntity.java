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
 * 照片原图及派生文件表
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
@TableName("t_photo_file")
public class PhotoFileEntity extends BaseField {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 照片ID */
    @TableField("photo_id")
    private Long photoId;

    /** 文件版本：ORIGINAL=原图，THUMBNAIL=缩略图，PREVIEW=预览图 */
    @TableField("file_type")
    private PhotoFileTypeEnum fileType;

    /** OSS对象路径，不直接作为公网下载地址 */
    @TableField("object_key")
    private String objectKey;

    /** 文件MIME类型 */
    @TableField("mime_type")
    private String mimeType;

    /** 文件实际大小（字节） */
    @TableField("size_bytes")
    private Long sizeBytes;

    /** 文件SHA-256 */
    @TableField("sha256")
    private String sha256;

    /** 文件宽度（像素） */
    @TableField("width_pixels")
    private Integer widthPixels;

    /** 文件高度（像素） */
    @TableField("height_pixels")
    private Integer heightPixels;

    /** 文件状态：AVAILABLE=可用，DELETE_PENDING=待永久删除，DELETE_FAILED=删除失败 */
    @TableField("status")
    private PhotoStatusEnum status;

    /** 删除原因：未删除时为空，USER_MANUAL=用户主动删除，ENTITLEMENT_EXPIRED=权益到期自动清理 */
    @TableField("delete_reason")
    private DeleteReasonEnum deleteReason;

    /** 对应OSS回调事件ID */
    @TableField("oss_callback_event_id")
    private String ossCallbackEventId;
}
