package com.app.support.oss;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * obs服务接口
 *
 * @since 2024-08-30
 */
public interface ObsFileService {
    /**
     * 存储桶存不存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 获取对象的元数据
     *
     * @param bucketName 存储桶名称。
     * @param objectName 存储桶里的对象名称
     * @return 对象的元数据
     */
    FileMetadata statObject(String bucketName, String objectName);

    /**
     * 上传文件对象
     *
     * @param bucketName    存储桶名称
     * @param fileName      文件名称
     * @param multipartFile 文件
     * @return 对象在桶内的名称（路径）
     */
    String putObject(String bucketName, String fileName, MultipartFile multipartFile);

    /**
     * 上传文件对象
     *
     * @param bucketName 存储桶名称
     * @param fileName   文件名称
     * @param file       文件
     * @return 对象在桶内的名称（路径）
     */
    String putObject(String bucketName, String fileName, File file);

    /**
     * 上传文件对象
     *
     * @param bucketName 存储桶名称
     * @param fileName   文件名称
     * @param fileByte   文件byte
     * @return 对象在桶内的名称（路径）
     */
    String putObjectByte(String bucketName, String fileName, byte[] fileByte);

    /**
     * 获取文件对象的文件流
     *
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @param offset     起始字节的位置
     * @param length     要读取的长度 (可选，如果无值则代表读到文件结尾)
     * @return 对象文件流
     */
    InputStream getObject(String bucketName, String objectName, Long offset, Long length);

    /**
     * 获取文件对象的文件流
     *
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @return 对象文件流
     */
    InputStream getObject(String bucketName, String objectName);

    /**
     * 删除对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @throws Exception 异常
     */
    void removeObject(String bucketName, String objectName) throws Exception;

    /**
     * 检查对象是否存在
     *
     * @param bucketName 存储桶名称。
     * @param objectName 存储桶里的对象名称
     * @return 是否存在
     */
    boolean existObject(String bucketName, String objectName);

    /**
     * 复制文件
     *
     * @param sourceBucketName 源桶
     * @param sourceObjectName 源对象
     * @param targetBucketName 目标桶
     * @param targetObjectName 目标文件
     */
    void copyObject(String sourceBucketName, String sourceObjectName, String targetBucketName, String targetObjectName);

    /**
     * 生成下载预签名 URL
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param expiration 过期时间
     * @return 预签名 GET URL
     */
    String generatePresignedDownloadUrl(String bucketName, String objectName, Duration expiration);

    /**
     * 生成带额外请求头的预签名URL
     *
     * @param bucketName
     * @param objectName
     * @param expiration
     * @param extParam
     * @param headers
     * @return
     */
    String generateUploadPresignedUrl(String bucketName, String objectName, Duration expiration, Map<String, String> extParam, Map<String, String> headers);
}