package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OSS上传完成回调请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class OssUploadCompletedRequest {
    /** 上传记录主键ID。 */
    private Long photoFileId;

    /** 上传对象路径。 */
    @NotBlank
    private String objectKey;

    /** 回调对象大小，单位为字节。 */
    private Long sizeBytes;

    /** 回调对象MIME类型。 */
    @NotBlank
    private String mimeType;

    /** 回调事件唯一编号。 */
    private String eventId;
}
