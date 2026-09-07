package com.app.service;

import com.app.enums.EntitlementSourceTypeEnum;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.StorageVo;
import com.app.service.impl.StorageServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StorageServiceImplTest {
    @Test
    void shouldRealtimeAggregateOnlyEffectiveEntitlementsAndClampRemaining() {
        StorageEntitlementMapper entitlementMapper = mock(StorageEntitlementMapper.class);
        UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        Clock clock = Clock.fixed(Instant.parse("2026-09-06T12:00:00Z"), ZoneOffset.UTC);
        StorageServiceImpl service = new StorageServiceImpl(entitlementMapper, accountMapper, clock);
        when(entitlementMapper.selectEffectiveByUserId(eq(7L), any())).thenReturn(List.of(
                entitlement("GIFT", EntitlementSourceTypeEnum.DEVICE_GIFT, 100L),
                entitlement("购买套餐", EntitlementSourceTypeEnum.PURCHASE, 200L)));
        when(accountMapper.selectByUserId(7L)).thenReturn(new UserStorageAccountEntity().setUsedBytes(250L).setReservedBytes(100L));

        StorageVo.Overview result = service.getOverview(7L);

        assertEquals(300L, result.getTotalCapacityBytes());
        assertEquals(250L, result.getUsedBytes());
        assertEquals(100L, result.getReservedBytes());
        assertEquals(0L, result.getRemainingBytes());
        assertEquals(LocalDateTime.of(2026, 9, 6, 12, 0), result.getNearestExpireTime());
        verify(entitlementMapper).selectEffectiveByUserId(7L, LocalDateTime.of(2026, 9, 6, 12, 0));
    }

    @Test
    void shouldReturnEffectiveEntitlementSnapshotsInExpiryOrder() {
        StorageEntitlementMapper entitlementMapper = mock(StorageEntitlementMapper.class);
        StorageServiceImpl service = new StorageServiceImpl(entitlementMapper, mock(UserStorageAccountMapper.class), Clock.systemUTC());
        StorageEntitlementEntity gift = entitlement("设备赠送", EntitlementSourceTypeEnum.DEVICE_GIFT, 100L);
        when(entitlementMapper.selectEffectiveByUserId(eq(7L), any())).thenReturn(List.of(gift));

        List<StorageVo.Entitlement> result = service.listEntitlements(7L);

        assertEquals(1, result.size());
        assertEquals("DEVICE_GIFT", result.getFirst().getSourceType());
        assertEquals("设备赠送", result.getFirst().getNameSnapshot());
        assertEquals(100L, result.getFirst().getCapacityBytes());
        assertEquals(gift.getEffectiveTime(), result.getFirst().getEffectiveTime());
        assertEquals(gift.getExpireTime(), result.getFirst().getExpireTime());
    }

    private static StorageEntitlementEntity entitlement(String name, EntitlementSourceTypeEnum source, long capacity) {
        return new StorageEntitlementEntity().setEntitlementNo("ENT001").setSourceType(source).setNameSnapshot(name)
                .setCapacityBytes(capacity).setEffectiveTime(LocalDateTime.of(2026, 9, 1, 0, 0))
                .setExpireTime(LocalDateTime.of(2026, 9, 6, 12, 0));
    }
}
