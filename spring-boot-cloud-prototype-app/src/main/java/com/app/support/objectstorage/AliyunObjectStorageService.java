package com.app.support.objectstorage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS对象元数据与删除操作
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class AliyunObjectStorageService implements ObjectStorageService {
    private final OSS oss;
    private final String bucket;

    public AliyunObjectStorageService(OSS oss, @Value("${oss.bucket}") String bucket) {
        this.oss = oss;
        this.bucket = bucket;
    }

    @Override
    public com.app.support.objectstorage.ObjectMetadata stat(String objectKey) {
        ObjectMetadata metadata = oss.getObjectMetadata(bucket, objectKey);
        return new com.app.support.objectstorage.ObjectMetadata().setObjectKey(objectKey).setSizeBytes(metadata.getContentLength())
                .setContentType(metadata.getContentType());
    }

    @Override
    public void delete(String objectKey) {
        oss.deleteObject(bucket, objectKey);
    }
}
