package com.app.service.impl;

import com.app.enums.EntitlementStatusEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.enums.PhotoStatusEnum;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.PhotoMapper;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.PhotoEntity;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.service.ExpirationCleanupService;
import com.app.support.cleanup.ExpirationCleanupLock;
import com.app.support.cleanup.ExpirationCleanupNotificationPublisher;
import com.app.support.objectstorage.ObjectStorageService;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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

/**
 * 权益到期清理服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class ExpirationCleanupServiceImplTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 8, 16, 30);

    static {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new org.apache.ibatis.session.Configuration(), "cleanup-photo");
        assistant.setCurrentNamespace("com.app.mapper.PhotoMapper");
        TableInfoHelper.initTableInfo(assistant, PhotoEntity.class);
        MapperBuilderAssistant fileAssistant = new MapperBuilderAssistant(new org.apache.ibatis.session.Configuration(), "cleanup-file");
        fileAssistant.setCurrentNamespace("com.app.mapper.PhotoFileMapper");
        TableInfoHelper.initTableInfo(fileAssistant, PhotoFileEntity.class);
    }

    @Test
    void shouldExpireWithoutDeletingWhenUsageFitsEffectiveCapacity() {
        Fixture fixture = fixture();
        StorageEntitlementEntity entitlement = entitlement(7L, 101L);
        when(fixture.entitlements.selectPendingExpiration(NOW, 100)).thenReturn(List.of(entitlement));
        when(fixture.entitlements.sumActiveCapacity(7L, NOW)).thenReturn(100L);
        when(fixture.accounts.selectByUserIdForUpdate(7L)).thenReturn(account(7L, 100L));
        when(fixture.entitlements.markExpiredProcessed(101L, NOW)).thenReturn(1);

        fixture.service.runCleanup();

        verify(fixture.photos, never()).selectList(any());
        verify(fixture.entitlements).markExpiredProcessed(101L, NOW);
        assertEquals(EntitlementStatusEnum.EXPIRED, entitlement.getStatus());
        verify(fixture.notifications).completed(eq(7L), eq("Asia/Shanghai"), eq(NOW), eq(false));
    }

    @Test
    void shouldDeleteOldestPhotoThenIdAndDecreaseUsageOnce() {
        Fixture fixture = fixture();
        when(fixture.entitlements.selectPendingExpiration(NOW, 100)).thenReturn(List.of(entitlement(7L, 101L)));
        when(fixture.entitlements.sumActiveCapacity(7L, NOW)).thenReturn(100L);
        when(fixture.accounts.selectByUserIdForUpdate(7L)).thenReturn(account(7L, 150L));
        PhotoEntity oldest = photo(1L, 60L, LocalDateTime.of(2026, 3, 1, 1, 0));
        when(fixture.photos.selectList(any())).thenReturn(List.of(oldest));
        when(fixture.files.selectList(any())).thenReturn(files(1L));
        when(fixture.photos.updateById(org.mockito.ArgumentMatchers.<PhotoEntity>any())).thenReturn(1);
        when(fixture.files.updateById(org.mockito.ArgumentMatchers.<PhotoFileEntity>any())).thenReturn(1);
        when(fixture.accounts.decreaseUsedBytes(7L, 60L)).thenReturn(1);
        when(fixture.entitlements.markExpiredProcessed(101L, NOW)).thenReturn(1);

        fixture.service.runCleanup();

        verify(fixture.storage).delete("original-1");
        verify(fixture.storage).delete("thumbnail-1");
        verify(fixture.storage).delete("preview-1");
        verify(fixture.accounts).decreaseUsedBytes(7L, 60L);
        verify(fixture.entitlements).markExpiredProcessed(101L, NOW);
    }

    @Test
    void shouldKeepDeleteFailedStateAndNotCompleteEntitlementWhenOssPartiallyFails() {
        Fixture fixture = fixture();
        when(fixture.entitlements.selectPendingExpiration(NOW, 100)).thenReturn(List.of(entitlement(7L, 101L)));
        when(fixture.entitlements.sumActiveCapacity(7L, NOW)).thenReturn(0L);
        when(fixture.accounts.selectByUserIdForUpdate(7L)).thenReturn(account(7L, 40L));
        PhotoEntity photo = photo(1L, 40L, NOW.minusDays(1));
        when(fixture.photos.selectList(any())).thenReturn(List.of(photo));
        when(fixture.files.selectList(any())).thenReturn(files(1L));
        when(fixture.photos.updateById(org.mockito.ArgumentMatchers.<PhotoEntity>any())).thenReturn(1);
        when(fixture.files.updateById(org.mockito.ArgumentMatchers.<PhotoFileEntity>any())).thenReturn(1);
        doThrow(new RuntimeException("oss down")).when(fixture.storage).delete("preview-1");

        fixture.service.runCleanup();

        assertEquals(PhotoStatusEnum.DELETE_FAILED, photo.getStatus());
        verify(fixture.accounts, never()).decreaseUsedBytes(any(), any());
        verify(fixture.entitlements, never()).markExpiredProcessed(any(), any());
    }

    @Test
    void shouldBeIdempotentWhenConditionalCompletionWasAlreadyWonByAnotherWorker() {
        Fixture fixture = fixture();
        when(fixture.entitlements.selectPendingExpiration(NOW, 100)).thenReturn(List.of(entitlement(7L, 101L)));
        when(fixture.entitlements.sumActiveCapacity(7L, NOW)).thenReturn(100L);
        when(fixture.accounts.selectByUserIdForUpdate(7L)).thenReturn(account(7L, 100L));
        when(fixture.entitlements.markExpiredProcessed(101L, NOW)).thenReturn(0);

        fixture.service.runCleanup();

        verify(fixture.entitlements).markExpiredProcessed(101L, NOW);
        verify(fixture.accounts, never()).decreaseUsedBytes(any(), any());
    }

    private Fixture fixture() {
        StorageEntitlementMapper entitlements = mock(StorageEntitlementMapper.class);
        PhotoMapper photos = mock(PhotoMapper.class);
        PhotoFileMapper files = mock(PhotoFileMapper.class);
        UserStorageAccountMapper accounts = mock(UserStorageAccountMapper.class);
        ObjectStorageService storage = mock(ObjectStorageService.class);
        ExpirationCleanupNotificationPublisher notifications = mock(ExpirationCleanupNotificationPublisher.class);
        ExpirationCleanupLock lock = new ExpirationCleanupLock() {
            @Override
            public <T> T execute(Callback<T> callback) {
                return callback.get();
            }
        };
        return new Fixture(entitlements, photos, files, accounts, storage, notifications,
                new ExpirationCleanupServiceImpl(entitlements, photos, files, accounts, storage, lock, notifications,
                        Clock.fixed(Instant.parse("2026-03-08T16:30:00Z"), ZoneOffset.UTC)));
    }

    private StorageEntitlementEntity entitlement(long userId, long id) {
        return new StorageEntitlementEntity().setId(id).setUserId(userId).setStatus(EntitlementStatusEnum.ACTIVE)
                .setUserTimeZoneSnapshot("Asia/Shanghai");
    }

    private UserStorageAccountEntity account(long userId, long used) {
        return new UserStorageAccountEntity().setUserId(userId).setUsedBytes(used);
    }

    private PhotoEntity photo(long id, long size, LocalDateTime uploaded) {
        return new PhotoEntity().setId(id).setUserId(7L).setOriginalSizeBytes(size).setUploadedTime(uploaded)
                .setStatus(PhotoStatusEnum.AVAILABLE);
    }

    private List<PhotoFileEntity> files(long photoId) {
        return List.of(file(photoId, PhotoFileTypeEnum.ORIGINAL), file(photoId, PhotoFileTypeEnum.THUMBNAIL),
                file(photoId, PhotoFileTypeEnum.PREVIEW));
    }

    private PhotoFileEntity file(long photoId, PhotoFileTypeEnum type) {
        return new PhotoFileEntity().setId((long) (photoId * 10 + type.ordinal())).setPhotoId(photoId).setFileType(type)
                .setObjectKey(type.name().toLowerCase() + "-" + photoId).setStatus(PhotoStatusEnum.AVAILABLE);
    }

    private record Fixture(StorageEntitlementMapper entitlements, PhotoMapper photos, PhotoFileMapper files,
                           UserStorageAccountMapper accounts, ObjectStorageService storage,
                           ExpirationCleanupNotificationPublisher notifications, ExpirationCleanupService service) {
    }
}
