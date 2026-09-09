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
    /** 原图照片文件记录主键ID。 */
    private Long photoFileId;

    /** MinIO对象路径。 */
    private String objectKey;

    /** 临时上传预签名URL。 */
    private String uploadUrl;

    /** 上传请求方法。 */
    private String method;

    /** 上传时必须携带的请求头。 */
    private Map<String, String> headers;

    /** 临时上传URL过期时间。 */
    private LocalDateTime expireTime;
}
