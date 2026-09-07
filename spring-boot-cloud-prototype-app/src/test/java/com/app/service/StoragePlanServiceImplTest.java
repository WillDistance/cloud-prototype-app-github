package com.app.service;

import com.app.enums.StoragePlanStatusEnum;
import com.app.mapper.StoragePlanMapper;
import com.app.pojo.entity.StoragePlanEntity;
import com.app.pojo.vo.StoragePlanVo;
import com.app.service.impl.StoragePlanServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StoragePlanServiceImplTest {
    @Test
    void shouldReturnRedisCachedActivePlansWithoutDatabaseQuery() throws Exception {
        StoragePlanMapper mapper = mock(StoragePlanMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        String cached = objectMapper.writeValueAsString(List.of(new StoragePlanVo.ActivePlan()
                .setPlanCode("BASIC").setPlanName("基础套餐").setCapacityBytes(1024L)));
        when(redis.opsForValue().get(anyString())).thenReturn(cached);
        StoragePlanServiceImpl service = new StoragePlanServiceImpl(mapper, redis, objectMapper,
                Clock.fixed(Instant.parse("2026-09-06T12:00:00Z"), ZoneOffset.UTC));

        List<StoragePlanVo.ActivePlan> result = service.listActivePlans();

        assertEquals(1, result.size());
        assertEquals("基础套餐", result.getFirst().getPlanName());
        verify(mapper, never()).selectActivePlans(any());
    }

    @Test
    void shouldQueryDatabaseAndPopulateRedisAfterCacheMissOrInvalidValue() {
        StoragePlanMapper mapper = mock(StoragePlanMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(redis.opsForValue().get(anyString())).thenReturn("invalid-json");
        when(mapper.selectActivePlans(any())).thenReturn(List.of(plan()));
        StoragePlanServiceImpl service = new StoragePlanServiceImpl(mapper, redis,
                JsonMapper.builder().findAndAddModules().build(), Clock.systemUTC());

        List<StoragePlanVo.ActivePlan> result = service.listActivePlans();

        assertEquals(1, result.size());
        verify(mapper).selectActivePlans(any());
        verify(redis.opsForValue()).set(anyString(), anyString(), any());
    }

    private static StoragePlanEntity plan() {
        return new StoragePlanEntity().setPlanCode("BASIC").setPlanVersion(1).setPlanName("基础套餐")
                .setCapacityBytes(1024L).setDurationValue(1).setPriceCent(100L).setRecommended(true)
                .setStatus(StoragePlanStatusEnum.ACTIVE).setEffectiveTime(LocalDateTime.of(2026, 9, 1, 0, 0));
    }
}
