package com.app.service;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.mapper.*;
import com.app.pojo.dto.DeviceUploadDto;
import com.app.pojo.entity.*;
import com.app.pojo.vo.DeviceUploadVo;
import com.app.service.impl.DeviceUploadServiceImpl;
import com.app.support.objectstorage.OssUploadUrlSigner;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 绑定设备上传会话服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class DeviceUploadServiceImplTest {
    @Test
    void shouldAuthenticateBoundDeviceReserveCapacityAndIssuePutUrl() throws Exception {
        Fixture f = new Fixture();
        f.prepareHappyPath();

        DeviceUploadVo.CreateSession result = f.service.createUploadSession(new DeviceUploadDto.CreateSession()
                .setDeviceId("CC-2026-AB12-8A2F").setPassword("secret")
                .setOriginalFileName("summer.JPG").setContentType("image/jpeg").setFileSizeBytes(200L));

        assertTrue(result.getUploadNo().startsWith("UPL"));
        assertEquals("https://oss.example/upload", result.getUploadUrl());
        assertEquals("PUT", result.getMethod());
        assertEquals("image/jpeg", result.getRequiredHeaders().get("Content-Type"));
        verify(f.accountMapper).increaseReservedBytes(7L, 200L);
        ArgumentCaptor<UploadSessionEntity> captor = ArgumentCaptor.forClass(UploadSessionEntity.class);
        verify(f.sessionMapper).insert(captor.capture());
        UploadSessionEntity session = captor.getValue();
        assertEquals(7L, session.getUserId());
        assertEquals(3L, session.getDeviceId());
        assertEquals(11L, session.getDeviceBindingId());
        assertEquals(UploadRequestSourceEnum.BOUND_DEVICE, session.getRequestSource());
        assertEquals(DeviceAuthTypeEnum.DEVICE_PASSWORD, session.getDeviceAuthType());
        assertEquals(UploadSessionStatusEnum.URL_ISSUED, session.getStatus());
        assertEquals(200L, session.getReservedBytes());
        assertTrue(session.getOriginalObjectKey().matches("users/7/devices/3/original/\\d{4}/\\d{2}/[0-9a-f]{32}\\.jpg"));
        verify(f.signer).createPutUrl(eq(session.getOriginalObjectKey()), eq("image/jpeg"), any(LocalDateTime.class));
    }

    @Test
    void shouldRejectInvalidAuthenticationBindingFileAndCapacity() {
        Fixture missing = new Fixture();
        assertCode(ErrorCodeEnum.DEVICE_NOT_FOUND, () -> missing.service.createUploadSession(missing.dto()));

        Fixture disabled = new Fixture(); disabled.prepareHappyPath();
        when(disabled.deviceMapper.selectByBusinessDeviceId("CC-2026-AB12-8A2F"))
                .thenReturn(disabled.device().setStatus(DeviceStatusEnum.DISABLED));
        assertCode(ErrorCodeEnum.DEVICE_DISABLED, () -> disabled.service.createUploadSession(disabled.dto()));

        Fixture wrong = new Fixture(); wrong.prepareHappyPath();
        when(wrong.encoder.matches("secret", "hash")).thenReturn(false);
        assertCode(ErrorCodeEnum.DEVICE_CREDENTIAL_INVALID, () -> wrong.service.createUploadSession(wrong.dto()));

        Fixture unbound = new Fixture(); unbound.prepareHappyPath();
        when(unbound.bindingMapper.selectByDeviceId(3L)).thenReturn(null);
        assertCode(ErrorCodeEnum.DEVICE_BINDING_NOT_FOUND, () -> unbound.service.createUploadSession(unbound.dto()));

        Fixture type = new Fixture(); type.prepareHappyPath();
        assertCode(ErrorCodeEnum.PHOTO_FILE_INVALID, () -> type.service.createUploadSession(type.dto().setOriginalFileName("a.exe")));
        assertCode(ErrorCodeEnum.PHOTO_FILE_INVALID, () -> type.service.createUploadSession(type.dto().setContentType("application/pdf")));
        assertCode(ErrorCodeEnum.PHOTO_FILE_INVALID, () -> type.service.createUploadSession(type.dto().setFileSizeBytes(0L)));
        assertCode(ErrorCodeEnum.PHOTO_FILE_INVALID, () -> type.service.createUploadSession(type.dto().setFileSizeBytes(1001L)));

        Fixture capacity = new Fixture(); capacity.prepareHappyPath();
        when(capacity.entitlementMapper.sumActiveCapacity(7L, capacity.now)).thenReturn(1099L);
        assertCode(ErrorCodeEnum.STORAGE_CAPACITY_INSUFFICIENT,
                () -> capacity.service.createUploadSession(capacity.dto()));
        verify(capacity.accountMapper, never()).increaseReservedBytes(anyLong(), anyLong());
    }

    @Test
    void shouldOnlyReturnSessionOwnedByAuthenticatedDevice() {
        Fixture f = new Fixture(); f.prepareHappyPath();
        UploadSessionEntity entity = new UploadSessionEntity().setUploadNo("UPL-1").setDeviceId(3L).setDeviceBindingId(11L)
                .setStatus(UploadSessionStatusEnum.PROCESSING).setOriginalFileName("a.jpg")
                .setDeclaredFileSizeBytes(200L).setUploadUrlExpireTime(f.now.plusMinutes(10));
        when(f.sessionMapper.selectByUploadNoAndDeviceId("UPL-1", 3L)).thenReturn(entity);

        DeviceUploadVo.SessionStatus result = f.service.getUploadSessionStatus(
                new DeviceUploadDto.SessionStatus().setDeviceId("CC-2026-AB12-8A2F")
                        .setPassword("secret").setUploadNo("UPL-1"));
        assertEquals("PROCESSING", result.getStatus());
        assertEquals("a.jpg", result.getOriginalFileName());
        assertNull(result.getObjectKey());

        when(f.sessionMapper.selectByUploadNoAndDeviceId("UPL-2", 3L)).thenReturn(null);
        assertCode(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID, () -> f.service.getUploadSessionStatus(
                new DeviceUploadDto.SessionStatus().setDeviceId("CC-2026-AB12-8A2F")
                        .setPassword("secret").setUploadNo("UPL-2")));
        verify(f.sessionMapper, never()).selectByUploadNo(anyString());
    }

    @Test
    void shouldReleaseExpiredReservationsOnce() {
        Fixture f = new Fixture();
        UploadSessionEntity expired = new UploadSessionEntity().setId(21L).setUserId(7L)
                .setReservedBytes(200L).setStatus(UploadSessionStatusEnum.URL_ISSUED);
        when(f.sessionMapper.selectExpiredForUpdate(f.now, 100)).thenReturn(List.of(expired));
        when(f.sessionMapper.markExpiredIfUrlIssued(21L)).thenReturn(1);
        when(f.accountMapper.selectByUserIdForUpdate(7L)).thenReturn(f.account());
        when(f.accountMapper.decreaseReservedBytes(7L, 200L)).thenReturn(1);

        assertEquals(1, f.service.releaseExpiredReservations(f.now, 100));
        verify(f.accountMapper).decreaseReservedBytes(7L, 200L);
    }

    @Test
    void transactionalMethodsMustRollbackOnAnyException() throws Exception {
        Transactional create = DeviceUploadServiceImpl.class.getMethod("createUploadSession", DeviceUploadDto.CreateSession.class)
                .getAnnotation(Transactional.class);
        Transactional release = DeviceUploadServiceImpl.class.getMethod("releaseExpiredReservations", LocalDateTime.class, int.class)
                .getAnnotation(Transactional.class);
        assertNotNull(create); assertEquals(Exception.class, create.rollbackFor()[0]);
        assertNotNull(release); assertEquals(Exception.class, release.rollbackFor()[0]);
    }

    @Test
    void ossSignerMustGenerateShortLivedPutOnlyUrlWithContentType() throws Exception {
        OSS oss = mock(OSS.class);
        when(oss.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("https://oss.example/signed"));
        OssUploadUrlSigner signer = new OssUploadUrlSigner(oss, "private-bucket");
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        assertEquals("https://oss.example/signed", signer.createPutUrl("safe/key.jpg", "image/jpeg", expiry));
        ArgumentCaptor<GeneratePresignedUrlRequest> captor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(oss).generatePresignedUrl(captor.capture());
        assertEquals(HttpMethod.PUT, captor.getValue().getMethod());
        assertEquals("private-bucket", captor.getValue().getBucketName());
        assertEquals("safe/key.jpg", captor.getValue().getKey());
        assertEquals("image/jpeg", captor.getValue().getContentType());
        assertEquals("image/jpeg", captor.getValue().getHeaders().get("Content-Type"));
        long seconds = Duration.between(LocalDateTime.now(), expiry).getSeconds();
        assertTrue(seconds > 0 && seconds <= 600);
    }

    private static void assertCode(ErrorCodeEnum code, Runnable action) {
        assertEquals(code, assertThrows(BusinessException.class, action::run).getErrorCode());
    }

    private static class Fixture {
        final LocalDateTime now = LocalDateTime.of(2026, 9, 6, 8, 0);
        final DeviceMapper deviceMapper = mock(DeviceMapper.class);
        final DeviceBindingMapper bindingMapper = mock(DeviceBindingMapper.class);
        final PlatformConfigMapper configMapper = mock(PlatformConfigMapper.class);
        final UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        final StorageEntitlementMapper entitlementMapper = mock(StorageEntitlementMapper.class);
        final UploadSessionMapper sessionMapper = mock(UploadSessionMapper.class);
        final PasswordEncoder encoder = mock(PasswordEncoder.class);
        final OssUploadUrlSigner signer = mock(OssUploadUrlSigner.class);
        final DeviceUploadServiceImpl service = new DeviceUploadServiceImpl(deviceMapper, bindingMapper, configMapper,
                accountMapper, entitlementMapper, sessionMapper, encoder, signer, () -> now, 600);

        void prepareHappyPath() {
            when(deviceMapper.selectByBusinessDeviceId("CC-2026-AB12-8A2F")).thenReturn(device());
            when(encoder.matches("secret", "hash")).thenReturn(true);
            when(bindingMapper.selectByDeviceId(3L)).thenReturn(new DeviceBindingEntity().setId(11L).setUserId(7L)
                    .setDeviceId(3L).setStatus(DeviceBindingStatusEnum.BOUND).setBindUserTimeZone("Asia/Shanghai"));
            when(configMapper.selectCurrentActive(now)).thenReturn(new PlatformConfigEntity().setMaxFileSizeBytes(1000L)
                    .setAllowedExtensions(List.of("jpg", "jpeg", "png")).setAllowedMimeTypes(List.of("image/jpeg", "image/png")));
            when(accountMapper.selectByUserIdForUpdate(7L)).thenReturn(account());
            when(entitlementMapper.sumActiveCapacity(7L, now)).thenReturn(1200L);
            when(signer.createPutUrl(anyString(), anyString(), any())).thenReturn("https://oss.example/upload");
        }
        DeviceEntity device() { return new DeviceEntity().setId(3L).setDeviceId("CC-2026-AB12-8A2F")
                .setInitialPasswordHash("hash").setStatus(DeviceStatusEnum.BOUND); }
        UserStorageAccountEntity account() { return new UserStorageAccountEntity().setId(5L).setUserId(7L)
                .setUsedBytes(800L).setReservedBytes(199L); }
        DeviceUploadDto.CreateSession dto() { return new DeviceUploadDto.CreateSession().setDeviceId("CC-2026-AB12-8A2F")
                .setPassword("secret").setOriginalFileName("a.jpg").setContentType("image/jpeg").setFileSizeBytes(200L); }
    }
}
