package com.app.config;

import com.app.support.cleanup.ExpirationCleanupLock;
import com.app.support.cleanup.ExpirationCleanupNotificationPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 权益到期清理基础设施配置。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Configuration
public class ExpirationCleanupConfiguration {
    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean(ExpirationCleanupLock.class)
    public ExpirationCleanupLock redisExpirationCleanupLock(StringRedisTemplate redisTemplate) {
        return new ExpirationCleanupLock() {
            @Override
            public <T> T execute(Callback<T> callback) {
                String token = UUID.randomUUID().toString();
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent("lock:storage:expiration-cleanup", token, Duration.ofMinutes(5));
                if (!Boolean.TRUE.equals(acquired)) return null;
                try {
                    return callback.get();
                } finally {
                    redisTemplate.execute(new DefaultRedisScript<>(
                                    "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end",
                                    Long.class),
                            Collections.singletonList("lock:storage:expiration-cleanup"), token);
                }
            }
        };
    }

    /**
     * 无Redis环境的单机替身，仅适用于单实例部署。
     */
    @Bean
    @ConditionalOnMissingBean({ExpirationCleanupLock.class, StringRedisTemplate.class})
    public ExpirationCleanupLock localExpirationCleanupLock() {
        return new ExpirationCleanupLock() {
            @Override
            public synchronized <T> T execute(Callback<T> callback) {
                return callback.get();
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(ExpirationCleanupNotificationPublisher.class)
    public ExpirationCleanupNotificationPublisher expirationCleanupNotificationPublisher() {
        return new ExpirationCleanupNotificationPublisher() {
        };
    }
}
