package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备上传预签名地址响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class DeviceUploadVo {
    private Long photoFileId;
    private String objectKey;
    private String uploadUrl;
    private String method;
    private Map<String, String> headers;
    private LocalDateTime expireTime;
}
