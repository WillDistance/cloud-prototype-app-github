package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备上传会话请求模型
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class DeviceUploadDto {
    @Data
    @Accessors(chain = true)
    public static class CreateSession {
        private String deviceId;
        private String password;
        @NotBlank private String originalFileName;
        @NotBlank private String contentType;
        @NotNull @Positive private Long fileSizeBytes;
    }

    @Data
    @Accessors(chain = true)
    public static class SessionStatus {
        private String deviceId;
        private String password;
        @NotBlank private String uploadNo;
    }
}
