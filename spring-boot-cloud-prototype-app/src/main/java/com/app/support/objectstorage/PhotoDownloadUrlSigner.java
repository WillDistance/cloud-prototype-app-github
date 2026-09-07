package com.app.support.objectstorage;

import java.time.LocalDateTime;

/**
 * 私有照片下载地址签发器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface PhotoDownloadUrlSigner {
    /**
     * 创建只读短时签名地址
     *
     * @param objectKey  OSS对象路径
     * @param expireTime 过期时间（UTC）
     * @return 签名下载地址
     */
    String createGetUrl(String objectKey, LocalDateTime expireTime);
}
