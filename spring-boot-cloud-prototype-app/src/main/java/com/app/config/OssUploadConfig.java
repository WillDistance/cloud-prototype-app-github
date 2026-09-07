package com.app.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.app.support.objectstorage.OssUploadUrlSigner;
import com.app.support.objectstorage.UploadUrlSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS上传签名配置
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Configuration
public class OssUploadConfig {
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(OSS.class)
    public OSS ossClient(@Value("${oss.endpoint}") String endpoint,
                         @Value("${oss.region}") String region,
                         @Value("${oss.access-key-id}") String accessKeyId,
                         @Value("${oss.access-key-secret}") String accessKeySecret) {
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 使用官方示例中的V4签名版本，确保预签名PUT URL与当前OSS区域配置匹配。
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(UploadUrlSigner.class)
    public UploadUrlSigner uploadUrlSigner(OSS oss, @Value("${oss.bucket}") String bucket,
                                           @Value("${oss.callback-url:}") String callbackUrl,
                                           @Value("${oss.callback-test-signature:}") String callbackSignature) {
        return new OssUploadUrlSigner(oss, bucket, callbackUrl, callbackSignature);
    }
}
