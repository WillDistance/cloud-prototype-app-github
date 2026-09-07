package com.app.support.objectstorage;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 阿里云OSS照片GET链接签发器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class OssPhotoDownloadUrlSigner implements PhotoDownloadUrlSigner {
    private final OSS oss;
    private final String bucketName;

    public OssPhotoDownloadUrlSigner(OSS oss, String bucketName) {
        this.oss = oss;
        this.bucketName = bucketName;
    }

    @Override
    public String createGetUrl(String objectKey, LocalDateTime expireTime) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey, HttpMethod.GET);
        request.setExpiration(java.util.Date.from(expireTime.toInstant(ZoneOffset.UTC)));
        return oss.generatePresignedUrl(request).toString();
    }
}
