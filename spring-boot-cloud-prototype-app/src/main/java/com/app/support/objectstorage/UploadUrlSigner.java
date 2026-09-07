package com.app.support.objectstorage;

import java.time.LocalDateTime;

/**
 * OSS上传链接签发器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface UploadUrlSigner {
    /**
     * 生成仅允许PUT上传、并在指定时间后失效的对象存储预签名URL。
     *
     * @param objectKey 对象存储文件键
     * @param contentType 上传文件的Content-Type
     * @param expireTime URL失效时间
     * @return 对象存储PUT预签名URL
     */
    String createPutUrl(String objectKey, String contentType, LocalDateTime expireTime);

    /**
     * 生成绑定当前上传会话回调信息的PUT预签名URL。
     *
     * @param objectKey 对象存储文件键
     * @param contentType 上传文件的Content-Type
     * @param expireTime URL失效时间
     * @param uploadNo 上传会话编号
     * @return 绑定回调信息的PUT预签名URL
     */
    default String createPutUrl(String objectKey, String contentType, LocalDateTime expireTime, String uploadNo) {
        return createPutUrl(objectKey, contentType, expireTime);
    }
}
