package com.app.config;

import com.app.support.ratelimit.InMemoryRateLimiter;
import com.app.support.ratelimit.RateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 安全能力默认实现配置。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Configuration
public class SecurityInfrastructureConfiguration {
    /**
     * 提供单机默认限流器，生产集群可注入Redis等实现替换。
     *
     * @return 限流器
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter rateLimiter() {
        return new InMemoryRateLimiter();
    }
}
