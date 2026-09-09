package com.app.support.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * MinIO配置实体
 *
 * @author yanlei
 * @since 2022-09-24
 */
@Data
@Configuration(value = "myObsProperties")
@ConfigurationProperties(prefix = "obs")
public class ObsProperties {

    /**
     * obs类型：MinIo、HwCloudObs
     */
    private String obsType;

    /**
     * minio地址+端口号
     */
    private String url;

    /**
     * minio用户名
     */
    private String accessKey;

    /**
     * minio密码
     */
    private String secretKey;

    /**
     * 文件名称
     */
    private String bucketName;

    /** 预签名下载地址有效期，默认3天。 */
    private Duration downloadUrlTtl = Duration.ofDays(3);
}
