package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备上传会话响应模型
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class DeviceUploadVo {
    @Data
    @Accessors(chain = true)
    public static class CreateSession {
        private String uploadNo;
        private String uploadUrl;
        private String method;
        private Map<String, String> requiredHeaders;
        private LocalDateTime expireTime;
    }

    @Data
    @Accessors(chain = true)
    public static class SessionStatus {
        private String uploadNo;
        private String status;
        private String originalFileName;
        private Long declaredFileSizeBytes;
        private LocalDateTime expireTime;
        private String objectKey;
    }
}
