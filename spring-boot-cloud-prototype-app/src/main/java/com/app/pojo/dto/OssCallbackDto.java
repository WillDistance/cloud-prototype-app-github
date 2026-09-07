package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OSS上传完成回调参数
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class OssCallbackDto {
    @NotBlank
    private String eventId;
    @NotBlank
    private String uploadNo;
    @NotBlank
    private String objectKey;
    @NotNull
    private Long sizeBytes;
    @NotBlank
    private String contentType;
    @NotBlank
    private String signature;
    @NotBlank
    private String callbackTime;
}
