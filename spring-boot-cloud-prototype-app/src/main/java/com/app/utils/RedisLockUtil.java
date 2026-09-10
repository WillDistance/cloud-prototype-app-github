package com.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * redis锁工具类
 *
 * @author yanlei
 * @since 2022-09-30
 */
@Slf4j
@Component
public class RedisLockUtil {

    /**
     * redis锁前缀
     */
    public static final String REDIS_LOCK_KEY_PREFIX = "redisLockPrefix:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 尝试获取redis锁
     *
     * @param businessKey 业务key
     * @param tryMaxCount 最大重试次数
     * @param lockTime    加锁时长
     * @param timeUnit    加锁时长单位
     * @return 锁value
     */
    public String tryGetLock(String businessKey, int tryMaxCount, long lockTime, TimeUnit timeUnit) {
        String redisKey = REDIS_LOCK_KEY_PREFIX + businessKey;
        String lockVal = UUID.randomUUID().toString();
        Boolean lockResult = false;
        try {
            do {
                lockResult = redisTemplate.opsForValue().setIfAbsent(redisKey, lockVal, lockTime, timeUnit);
                if (!lockResult) {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            } while (!lockResult && --tryMaxCount > 0);
        } catch (Exception e) {
            log.error("tryGetLock sleep exception,error={}", e);
        }
        return lockResult ? lockVal : null;
    }

    /**
     * 释放锁
     *
     * @param businessKey 业务key
     * @param lockVal     锁val
     * @return 释放结果
     */
    public boolean releaseLock(String businessKey, String lockVal) {
        if (StringUtils.isBlank(lockVal)) {
            return Boolean.FALSE;
        }
        String redisKey = REDIS_LOCK_KEY_PREFIX + businessKey;
        Long delNum = -1L;
        try {
            RedisScript<Long> script = RedisScript.of("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
            delNum = redisTemplate.execute(script, Collections.singletonList(redisKey), lockVal);
        } catch (Exception e) {
            log.error("releaseLock exception,businessKey={},lockVal={},error={}", businessKey, lockVal, e);
        }
        return delNum >= 1;
    }
}