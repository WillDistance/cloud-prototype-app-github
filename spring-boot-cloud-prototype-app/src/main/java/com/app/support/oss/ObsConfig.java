package com.app.support.oss;

import com.app.support.oss.hwcloud.HwCloudObsServiceImpl;
import com.app.support.oss.minio.MinioObsServiceImpl;
import com.obs.services.ObsClient;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 华为云Obs客户端 config
 *
 * @author yanlei
 * @since 2022-09-24
 */
@Slf4j
@Configuration
public class ObsConfig {

    @Autowired
    private ObsProperties obsProperties;

    /**
     * HwCloudObs客户端创建
     *
     * @return ObsClient
     */
    @Bean
    @ConditionalOnProperty(prefix = "obs", name = "obsType", havingValue = "HwCloudObs", matchIfMissing = false)
    public ObsClient createObsClient() {
        log.info("ObsConfig create HwCloudObs client");
        return new ObsClient(obsProperties.getAccessKey(), obsProperties.getSecretKey(), obsProperties.getUrl());
    }

    /**
     * 华为云的obs服务封装实现
     *
     * @param obsClient     obsClient
     * @param obsProperties obsProperties
     * @return HwCloudObsServiceImpl
     */
    @Bean
    @ConditionalOnBean(ObsClient.class)
    @ConditionalOnProperty(prefix = "obs", name = "obsType", havingValue = "HwCloudObs", matchIfMissing = false)
    public HwCloudObsServiceImpl hwCloudObsService(ObsClient obsClient, ObsProperties obsProperties) {
        log.info("ObsConfig create hwCloudObsService");
        return new HwCloudObsServiceImpl(obsClient, obsProperties);
    }

    /**
     * MinIo客户端创建
     *
     * @return MinioClient
     */
    @Bean
    @ConditionalOnProperty(prefix = "obs", name = "obsType", havingValue = "MinIo", matchIfMissing = false)
    public MinioClient minioClient() {
        log.info("ObsConfig create MinIo client");
        MinioClient minioClient = MinioClient.builder()
                .endpoint(obsProperties.getUrl())
                .credentials(obsProperties.getAccessKey(), obsProperties.getSecretKey())
                .build();
        return minioClient;
    }

    /**
     * MinIo的obs服务封装实现
     *
     * @param minioClient   minioClient
     * @param obsProperties obsProperties
     * @return MinioObsServiceImpl
     */
    @Bean
    @ConditionalOnBean(MinioClient.class)
    @ConditionalOnProperty(prefix = "obs", name = "obsType", havingValue = "MinIo", matchIfMissing = false)
    public MinioObsServiceImpl minioObsService(MinioClient minioClient, ObsProperties obsProperties) {
        log.info("ObsConfig create minioObsService");
        return new MinioObsServiceImpl(minioClient, obsProperties);
    }
}