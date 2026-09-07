package com.app.service.impl;

import com.app.mapper.StoragePlanMapper;
import com.app.pojo.entity.StoragePlanEntity;
import com.app.pojo.vo.StoragePlanVo;
import com.app.service.StoragePlanService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 存储套餐查询服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class StoragePlanServiceImpl implements StoragePlanService {
    private static final String CACHE_KEY = "storage:plans:active";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private final StoragePlanMapper planMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public StoragePlanServiceImpl(StoragePlanMapper planMapper, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this(planMapper, redisTemplate, objectMapper, Clock.systemUTC());
    }

    public StoragePlanServiceImpl(StoragePlanMapper planMapper, StringRedisTemplate redisTemplate,
                                  ObjectMapper objectMapper, Clock clock) {
        this.planMapper = planMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public List<StoragePlanVo.ActivePlan> listActivePlans() {
        String cached = null;
        try {
            cached = redisTemplate.opsForValue().get(CACHE_KEY);
        } catch (RuntimeException ignored) {
            // Redis异常时回源数据库。
        }
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<>() {
                });
            } catch (Exception ignored) {
                try {
                    redisTemplate.delete(CACHE_KEY);
                } catch (RuntimeException ignoredDelete) {
                    // 缓存删除失败不影响数据库回源。
                }
            }
        }
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        List<StoragePlanVo.ActivePlan> result = planMapper.selectActivePlans(now).stream().map(this::toVo).toList();
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(result), CACHE_TTL);
        } catch (Exception ignored) {
            // Redis不可用或序列化失败时仍以数据库查询结果为准。
        }
        return result;
    }

    /**
     * 将业务实体转换为前端视图对象。
     *
     * @param entity 数据库实体
     * @return 方法处理后的结果
     */
    private StoragePlanVo.ActivePlan toVo(StoragePlanEntity entity) {
        return new StoragePlanVo.ActivePlan().setPlanCode(entity.getPlanCode()).setPlanVersion(entity.getPlanVersion())
                .setPlanName(entity.getPlanName()).setCapacityBytes(entity.getCapacityBytes())
                .setDurationValue(entity.getDurationValue())
                .setDurationUnit(entity.getDurationUnit() == null ? null : entity.getDurationUnit().getValue())
                .setPriceCent(entity.getPriceCent()).setRecommended(entity.getRecommended())
                .setEffectiveTime(entity.getEffectiveTime());
    }
}
