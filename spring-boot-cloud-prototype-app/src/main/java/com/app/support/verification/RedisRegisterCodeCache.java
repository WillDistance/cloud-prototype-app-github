package com.app.support.verification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 注册验证码Redis状态存储实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class RedisRegisterCodeCache implements RegisterCodeCache {
    private static final String PREFIX = "auth:register:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRegisterCodeCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean requestCode(String email, CodeState state) {
        String rateKey = PREFIX + "rate:" + email;
        Boolean allowed = redisTemplate.opsForValue().setIfAbsent(rateKey, "1", SEND_INTERVAL);
        if (!Boolean.TRUE.equals(allowed)) {
            return false;
        }
        try {
            RedisState redisState = new RedisState(state.recordId(), state.codeHash(), 0, false);
            redisTemplate.opsForValue().set(PREFIX + "code:" + email, objectMapper.writeValueAsString(redisState), CODE_TTL);
            return true;
        } catch (JsonProcessingException exception) {
            redisTemplate.delete(rateKey);
            throw new IllegalStateException("验证码状态序列化失败", exception);
        }
    }

    @Override
    public VerifyResult verifyCode(String email, String code, PasswordEncoder passwordEncoder) {
        String key = PREFIX + "code:" + email;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return VerifyResult.EXPIRED;
        try {
            RedisState state = objectMapper.readValue(value, RedisState.class);
            if (state.attempts() >= MAX_ATTEMPTS) return VerifyResult.ATTEMPTS_EXCEEDED;
            if (!passwordEncoder.matches(code, state.codeHash())) {
                RedisState failed = new RedisState(state.recordId(), state.codeHash(), state.attempts() + 1, false);
                Duration ttl = Duration.ofMillis(Math.max(1, redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.MILLISECONDS)));
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(failed), ttl);
                return failed.attempts() >= MAX_ATTEMPTS ? VerifyResult.ATTEMPTS_EXCEEDED : VerifyResult.INVALID;
            }
            Duration ttl = Duration.ofMillis(Math.max(1, redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.MILLISECONDS)));
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(new RedisState(state.recordId(), state.codeHash(), state.attempts(), true)), ttl);
            return VerifyResult.VERIFIED;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("验证码状态解析失败", exception);
        }
    }

    @Override
    public boolean consumeVerified(String email) {
        String scriptText = "local v=redis.call('get',KEYS[1]); if not v then return 0 end; " +
                "if string.find(v,'\\\"verified\\\":true') then redis.call('del',KEYS[1]); return 1 end; return 0";
        Long result = redisTemplate.execute(new DefaultRedisScript<>(scriptText, Long.class), List.of(PREFIX + "code:" + email));
        return Long.valueOf(1L).equals(result);
    }

    public record RedisState(Long recordId, String codeHash, int attempts, boolean verified) {
    }
}
