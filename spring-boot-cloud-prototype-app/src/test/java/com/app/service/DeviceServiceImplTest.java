package com.app.service;

import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.mapper.*;
import com.app.pojo.dto.DeviceDto;
import com.app.pojo.entity.*;
import com.app.pojo.vo.DeviceVo;
import com.app.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 设备永久绑定服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class DeviceServiceImplTest {

    @Test
    void shouldBindDeviceAndCreateGiftAndStorageAccount() {
        Fixture fixture = new Fixture();
        fixture.prepareAvailableDevice(DurationUnitEnum.MONTH, 1);

        DeviceVo.BindResult result = fixture.service.bindDevice(7L,
                new DeviceDto.BindDevice().setDeviceId("CC-2026-AB12-8A2F").setPassword("device-secret"));

        assertEquals(11L, result.getBindingId());
        assertEquals("CC-2026-AB12-8A2F", result.getDeviceId());
        assertEquals("BOUND", result.getStatus());
        assertNotNull(result.getGiftExpireTime());

        ArgumentCaptor<DeviceBindingEntity> bindingCaptor = ArgumentCaptor.forClass(DeviceBindingEntity.class);
        verify(fixture.bindingMapper).insert(bindingCaptor.capture());
        DeviceBindingEntity binding = bindingCaptor.getValue();
        assertEquals(7L, binding.getUserId());
        assertEquals(3L, binding.getDeviceId());
        assertEquals("America/New_York", binding.getBindUserTimeZone());

        ArgumentCaptor<StorageEntitlementEntity> entitlementCaptor = ArgumentCaptor.forClass(StorageEntitlementEntity.class);
        verify(fixture.entitlementMapper).insert(entitlementCaptor.capture());
        StorageEntitlementEntity entitlement = entitlementCaptor.getValue();
        assertEquals(EntitlementSourceTypeEnum.DEVICE_GIFT, entitlement.getSourceType());
        assertEquals(11L, entitlement.getDeviceBindingId());
        assertEquals(1024L, entitlement.getCapacityBytes());
        assertEquals("America/New_York", entitlement.getUserTimeZoneSnapshot());
        assertTrue(entitlement.getEntitlementNo().startsWith("ENT"));
        assertEquals(entitlement.getEffectiveTime().atOffset(ZoneOffset.UTC).toInstant()
                        .atZone(ZoneId.of("America/New_York")).plusMonths(1).toInstant(),
                entitlement.getExpireTime().atOffset(ZoneOffset.UTC).toInstant());

        ArgumentCaptor<UserStorageAccountEntity> accountCaptor = ArgumentCaptor.forClass(UserStorageAccountEntity.class);
        verify(fixture.accountMapper).insert(accountCaptor.capture());
        assertEquals(0L, accountCaptor.getValue().getUsedBytes());
        assertEquals(0L, accountCaptor.getValue().getReservedBytes());
        assertEquals(0L, accountCaptor.getValue().getLockVersion());
        verify(fixture.deviceMapper).markBoundIfUnbound(3L, DeviceStatusEnum.UNBOUND, DeviceStatusEnum.BOUND);
    }

    @Test
    void shouldRejectInvalidDeviceIdBeforeQueryingDatabase() {
        Fixture fixture = new Fixture();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> fixture.service.bindDevice(7L, new DeviceDto.BindDevice().setDeviceId("bad-id").setPassword("secret")));
        assertEquals(ErrorCodeEnum.DEVICE_ID_INVALID, exception.getErrorCode());
        verify(fixture.bindingMapper, never()).selectUserForUpdate(any());
    }

    @Test
    void shouldRejectMissingWrongPasswordDisabledAndExistingBindings() {
        Fixture missing = new Fixture();
        when(missing.bindingMapper.selectUserForUpdate(7L)).thenReturn(user());
        when(missing.deviceMapper.selectByDeviceIdForUpdate("CC-2026-AB12-8A2F")).thenReturn(null);
        assertCode(ErrorCodeEnum.DEVICE_NOT_FOUND, () -> missing.service.bindDevice(7L, dto()));

        Fixture wrongPassword = new Fixture();
        wrongPassword.prepareAvailableDevice(DurationUnitEnum.DAY, 1);
        when(wrongPassword.encoder.matches("device-secret", "device-hash")).thenReturn(false);
        assertCode(ErrorCodeEnum.DEVICE_CREDENTIAL_INVALID, () -> wrongPassword.service.bindDevice(7L, dto()));

        Fixture disabled = new Fixture();
        disabled.prepareAvailableDevice(DurationUnitEnum.DAY, 1);
        when(disabled.deviceMapper.selectByDeviceIdForUpdate("CC-2026-AB12-8A2F"))
                .thenReturn(device().setStatus(DeviceStatusEnum.DISABLED));
        assertCode(ErrorCodeEnum.DEVICE_DISABLED, () -> disabled.service.bindDevice(7L, dto()));

        Fixture userBound = new Fixture();
        userBound.prepareAvailableDevice(DurationUnitEnum.DAY, 1);
        when(userBound.bindingMapper.selectByUserId(7L)).thenReturn(new DeviceBindingEntity());
        assertCode(ErrorCodeEnum.USER_ALREADY_BOUND_DEVICE, () -> userBound.service.bindDevice(7L, dto()));

        Fixture deviceBound = new Fixture();
        deviceBound.prepareAvailableDevice(DurationUnitEnum.DAY, 1);
        when(deviceBound.bindingMapper.selectByDeviceId(3L)).thenReturn(new DeviceBindingEntity());
        assertCode(ErrorCodeEnum.DEVICE_ALREADY_BOUND, () -> deviceBound.service.bindDevice(7L, dto()));
    }

    @Test
    void shouldMapDuplicateKeyToBindingConflict() {
        Fixture fixture = new Fixture();
        fixture.prepareAvailableDevice(DurationUnitEnum.YEAR, 1);
        when(fixture.bindingMapper.insert(any(DeviceBindingEntity.class))).thenThrow(new DuplicateKeyException("unique"));
        assertCode(ErrorCodeEnum.DEVICE_BINDING_CONFLICT, () -> fixture.service.bindDevice(7L, dto()));
    }

    @Test
    void bindMustDeclareRollbackTransactionContract() throws Exception {
        Transactional transactional = DeviceServiceImpl.class
                .getMethod("bindDevice", Long.class, DeviceDto.BindDevice.class)
                .getAnnotation(Transactional.class);
        assertNotNull(transactional);
        assertEquals(Exception.class, transactional.rollbackFor()[0]);
    }

    private static DeviceDto.BindDevice dto() {
        return new DeviceDto.BindDevice().setDeviceId("CC-2026-AB12-8A2F").setPassword("device-secret");
    }

    private static UserEntity user() {
        return new UserEntity().setId(7L).setTimeZone("America/New_York");
    }

    private static DeviceEntity device() {
        return new DeviceEntity().setId(3L).setDeviceId("CC-2026-AB12-8A2F")
                .setInitialPasswordHash("device-hash").setStatus(DeviceStatusEnum.UNBOUND).setModel("CAM-1");
    }

    private static void assertCode(ErrorCodeEnum code, Runnable runnable) {
        assertEquals(code, assertThrows(BusinessException.class, runnable::run).getErrorCode());
    }

    private static class Fixture {
        private final DeviceMapper deviceMapper = mock(DeviceMapper.class);
        private final DeviceBindingMapper bindingMapper = mock(DeviceBindingMapper.class);
        private final PlatformConfigMapper configMapper = mock(PlatformConfigMapper.class);
        private final StorageEntitlementMapper entitlementMapper = mock(StorageEntitlementMapper.class);
        private final UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        private final PasswordEncoder encoder = mock(PasswordEncoder.class);
        private final DeviceServiceImpl service = new DeviceServiceImpl(deviceMapper, bindingMapper, configMapper,
                entitlementMapper, accountMapper, encoder);

        private void prepareAvailableDevice(DurationUnitEnum unit, int value) {
            when(bindingMapper.selectUserForUpdate(7L)).thenReturn(user());
            when(deviceMapper.selectByDeviceIdForUpdate("CC-2026-AB12-8A2F")).thenReturn(device());
            when(encoder.matches("device-secret", "device-hash")).thenReturn(true);
            when(deviceMapper.markBoundIfUnbound(3L, DeviceStatusEnum.UNBOUND, DeviceStatusEnum.BOUND)).thenReturn(1);
            when(configMapper.selectCurrentActive(any())).thenReturn(new PlatformConfigEntity().setId(5L)
                    .setStatus(PlatformConfigStatusEnum.ACTIVE).setDeviceGiftCapacityBytes(1024L)
                    .setDeviceGiftDurationValue(value).setDeviceGiftDurationUnit(unit));
            when(bindingMapper.insert(any(DeviceBindingEntity.class))).thenAnswer(invocation -> {
                invocation.getArgument(0, DeviceBindingEntity.class).setId(11L);
                return 1;
            });
        }
    }
}
