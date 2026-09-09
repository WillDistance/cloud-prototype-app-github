package com.app.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备上传原图请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class DeviceUploadRequest {
    /** 设备业务编号。 */
    @NotBlank
    private String deviceId;

    /** 设备认证密码。 */
    @NotBlank
    private String password;

    /** 原始文件名称。 */
    @NotBlank
    private String fileName;

    /** 文件扩展名。 */
    @NotBlank
    private String extension;

    /** 文件MIME类型。 */
    @NotBlank
    private String mimeType;

    /** 原图大小，单位为字节。 */
    @Min(1)
    private Long sizeBytes;
}
