package com.app.config;

import com.aliyun.oss.OSS;
import com.app.support.objectstorage.OssPhotoDownloadUrlSigner;
import com.app.support.objectstorage.PhotoDownloadUrlSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS照片下载签名配置
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Configuration
public class OssPhotoDownloadConfig {
    @Bean
    @ConditionalOnMissingBean(PhotoDownloadUrlSigner.class)
    public PhotoDownloadUrlSigner photoDownloadUrlSigner(OSS oss, @Value("${oss.bucket}") String bucket) {
        return new OssPhotoDownloadUrlSigner(oss, bucket);
    }
}
