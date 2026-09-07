package com.app.service;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.enums.PhotoStatusEnum;
import com.app.enums.UploadSessionStatusEnum;
import com.app.exception.BusinessException;
import com.app.mapper.*;
import com.app.pojo.dto.OssCallbackDto;
import com.app.pojo.entity.*;
import com.app.pojo.vo.OssCallbackVo;
import com.app.service.impl.OssCallbackServiceImpl;
import com.app.support.objectstorage.ObjectMetadata;
import com.app.support.objectstorage.ObjectStorageService;
import com.app.support.oss.OssCallbackVerifier;
import com.app.support.photo.ImageProcessingResult;
import com.app.support.photo.PhotoImageProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * OSS上传完成回调服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class OssCallbackServiceImplTest {

    @Test
    void 发布冲突必须回滚而已记录的拒绝与处理失败应保留状态() throws Exception {
        Transactional transactional = OssCallbackServiceImpl.class
                .getMethod("uploadCompleted", OssCallbackDto.class)
                .getAnnotation(Transactional.class);

        assertTrue(transactional != null);
        assertFalse(List.of(transactional.noRollbackFor()).contains(BusinessException.class));
        assertTrue(List.of(transactional.noRollbackFor()).stream()
                .anyMatch(type -> type.getSimpleName().equals("OssCallbackRejectedException")));
        assertTrue(List.of(transactional.noRollbackFor()).stream()
                .anyMatch(type -> type.getSimpleName().equals("PhotoProcessingRetryableException")));
    }

    @Test
    void 合法回调应创建三文件并原子发布且预留转用量() {
        Fixture f = new Fixture();
        f.happyPath();

        OssCallbackVo result = f.service.uploadCompleted(f.dto());

        assertEquals("COMPLETED", result.getStatus());
        assertTrue(result.getPhotoNo().startsWith("PHT"));
        ArgumentCaptor<PhotoEntity> photo = ArgumentCaptor.forClass(PhotoEntity.class);
        verify(f.photoMapper).insert(photo.capture());
        assertEquals(PhotoStatusEnum.AVAILABLE, photo.getValue().getStatus());
        ArgumentCaptor<PhotoFileEntity> files = ArgumentCaptor.forClass(PhotoFileEntity.class);
        verify(f.photoFileMapper, times(3)).insert(files.capture());
        assertEquals(List.of(PhotoFileTypeEnum.ORIGINAL, PhotoFileTypeEnum.THUMBNAIL, PhotoFileTypeEnum.PREVIEW),
                files.getAllValues().stream().map(PhotoFileEntity::getFileType).toList());
        verify(f.accountMapper).moveReservedToUsed(7L, 200L, 200L);
        verify(f.sessionMapper).markCompletedIfProcessing(21L, f.now);
    }

    @Test
    void 重复回调不得重复生成或增加用量() {
        Fixture f = new Fixture();
        UploadSessionEntity completed = f.session().setStatus(UploadSessionStatusEnum.COMPLETED);
        when(f.sessionMapper.selectByUploadNoForUpdate("UPL001")).thenReturn(completed);
        when(f.photoMapper.selectByUploadSessionId(21L)).thenReturn(new PhotoEntity().setId(31L).setPhotoNo("PHT001"));

        OssCallbackVo result = f.service.uploadCompleted(f.dto());

        assertEquals("PHT001", result.getPhotoNo());
        assertTrue(result.getIdempotent());
        verify(f.processor, never()).process(any(), any(), any());
        verify(f.photoMapper, never()).insert(any(PhotoEntity.class));
        verify(f.accountMapper, never()).moveReservedToUsed(anyLong(), anyLong(), anyLong());
    }

    @Test
    void 对象Key不匹配应失败释放并清理对象() {
        Fixture f = new Fixture();
        when(f.sessionMapper.selectByUploadNoForUpdate("UPL001")).thenReturn(f.session());
        when(f.storage.stat("foreign.jpg")).thenReturn(f.metadata());
        when(f.sessionMapper.markFailedAndReleaseIfReserved(21L, "OBJECT_KEY_MISMATCH", "对象Key与上传会话不匹配")).thenReturn(1);
        when(f.accountMapper.decreaseReservedBytes(7L, 200L)).thenReturn(1);

        BusinessException error = assertThrows(BusinessException.class,
                () -> f.service.uploadCompleted(f.dto().setObjectKey("foreign.jpg")));

        assertEquals(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID, error.getErrorCode());
        verify(f.sessionMapper).markFailedAndReleaseIfReserved(21L, "OBJECT_KEY_MISMATCH", "对象Key与上传会话不匹配");
        verify(f.accountMapper).decreaseReservedBytes(7L, 200L);
        verify(f.storage).delete("foreign.jpg");
    }

    @Test
    void 实际大小超过声明值或平台上限应失败释放并清理() {
        Fixture f = new Fixture();
        when(f.sessionMapper.selectByUploadNoForUpdate("UPL001")).thenReturn(f.session());
        when(f.storage.stat("users/7/original/a.jpg")).thenReturn(f.metadata().setSizeBytes(201L));
        when(f.configMapper.selectCurrentActive(f.now)).thenReturn(new PlatformConfigEntity().setMaxFileSizeBytes(1000L));
        when(f.sessionMapper.markFailedAndReleaseIfReserved(21L, "FILE_METADATA_INVALID", "OSS对象元数据不符合上传会话约束")).thenReturn(1);
        when(f.accountMapper.decreaseReservedBytes(7L, 200L)).thenReturn(1);

        BusinessException error = assertThrows(BusinessException.class, () -> f.service.uploadCompleted(f.dto()));

        assertEquals(ErrorCodeEnum.PHOTO_FILE_INVALID, error.getErrorCode());
        verify(f.accountMapper).decreaseReservedBytes(7L, 200L);
        verify(f.storage).delete("users/7/original/a.jpg");
        verify(f.photoMapper, never()).insert(any(PhotoEntity.class));
    }

    @Test
    void 派生图失败时照片不可见且会话保持可重试并清理孤儿对象() {
        Fixture f = new Fixture();
        f.happyPath();
        doThrow(new IllegalStateException("preview failed")).when(f.processor).process(any(), any(), any());

        BusinessException error = assertThrows(BusinessException.class, () -> f.service.uploadCompleted(f.dto()));

        assertEquals(ErrorCodeEnum.PHOTO_PROCESSING_FAILED, error.getErrorCode());
        verify(f.sessionMapper).markRetryableFailedIfProcessing(21L, "IMAGE_PROCESSING_FAILED", "preview failed");
        verify(f.photoMapper, never()).insert(any(PhotoEntity.class));
        verify(f.photoFileMapper, never()).insert(any(PhotoFileEntity.class));
        verify(f.accountMapper, never()).decreaseReservedBytes(anyLong(), anyLong());
        verify(f.storage).delete("users/7/thumbnail/a.jpg");
        verify(f.storage).delete("users/7/preview/a.jpg");
    }

    @Test
    void 清理接口应删除失败会话的原图与派生孤儿对象并释放仍有预留() {
        Fixture f = new Fixture();
        UploadSessionEntity failed = f.session().setStatus(UploadSessionStatusEnum.FAILED);
        when(f.sessionMapper.selectFailedForCleanup(f.now, 10)).thenReturn(List.of(failed));
        when(f.sessionMapper.markCleanupReleasedIfFailed(21L)).thenReturn(1);
        when(f.accountMapper.selectByUserIdForUpdate(7L)).thenReturn(new UserStorageAccountEntity().setUserId(7L));
        when(f.accountMapper.decreaseReservedBytes(7L, 200L)).thenReturn(1);

        assertEquals(1, f.service.cleanupOrphanObjects(f.now, 10));

        verify(f.storage).delete("users/7/original/a.jpg");
        verify(f.storage).delete("users/7/thumbnail/a.jpg");
        verify(f.storage).delete("users/7/preview/a.jpg");
        verify(f.accountMapper).decreaseReservedBytes(7L, 200L);
    }

    private static class Fixture {
        final LocalDateTime now = LocalDateTime.of(2026, 9, 6, 9, 0);
        final UploadSessionMapper sessionMapper = mock(UploadSessionMapper.class);
        final PhotoMapper photoMapper = mock(PhotoMapper.class);
        final PhotoFileMapper photoFileMapper = mock(PhotoFileMapper.class);
        final UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        final PlatformConfigMapper configMapper = mock(PlatformConfigMapper.class);
        final OssCallbackVerifier verifier = mock(OssCallbackVerifier.class);
        final ObjectStorageService storage = mock(ObjectStorageService.class);
        final PhotoImageProcessor processor = mock(PhotoImageProcessor.class);
        final OssCallbackServiceImpl service = new OssCallbackServiceImpl(sessionMapper, photoMapper, photoFileMapper,
                accountMapper, configMapper, verifier, storage, processor, () -> now);

        void happyPath() {
            when(verifier.verify(any())).thenReturn(true);
            when(sessionMapper.selectByUploadNoForUpdate("UPL001")).thenReturn(session());
            when(storage.stat("users/7/original/a.jpg")).thenReturn(metadata());
            when(configMapper.selectCurrentActive(now)).thenReturn(new PlatformConfigEntity().setMaxFileSizeBytes(1000L));
            when(sessionMapper.markOriginalUploadedIfUrlIssued(21L, "evt-1")).thenReturn(1);
            when(sessionMapper.markProcessingIfUploadedOrRetryableFailed(21L)).thenReturn(1);
            when(processor.process("users/7/original/a.jpg", "users/7/thumbnail/a.jpg", "users/7/preview/a.jpg"))
                    .thenReturn(new ImageProcessingResult(metadata(),
                            new ObjectMetadata().setObjectKey("users/7/thumbnail/a.jpg").setSizeBytes(40L).setContentType("image/jpeg").setWidthPixels(480).setHeightPixels(320).setSha256("thumb"),
                            new ObjectMetadata().setObjectKey("users/7/preview/a.jpg").setSizeBytes(100L).setContentType("image/jpeg").setWidthPixels(1920).setHeightPixels(1280).setSha256("preview")));
            when(photoMapper.insert(any(PhotoEntity.class))).thenAnswer(invocation -> {
                ((PhotoEntity) invocation.getArgument(0)).setId(31L);
                return 1;
            });
            when(photoFileMapper.insert(any(PhotoFileEntity.class))).thenReturn(1);
            when(accountMapper.selectByUserIdForUpdate(7L)).thenReturn(new UserStorageAccountEntity().setUserId(7L));
            when(accountMapper.moveReservedToUsed(7L, 200L, 200L)).thenReturn(1);
            when(sessionMapper.markCompletedIfProcessing(21L, now)).thenReturn(1);
        }

        OssCallbackDto dto() {
            when(verifier.verify(any())).thenReturn(true);
            return new OssCallbackDto().setEventId("evt-1").setUploadNo("UPL001")
                    .setObjectKey("users/7/original/a.jpg").setSizeBytes(200L)
                    .setContentType("image/jpeg").setSignature("test-signature").setCallbackTime("2026-09-06T09:00:00Z");
        }

        UploadSessionEntity session() {
            return new UploadSessionEntity().setId(21L).setUploadNo("UPL001").setUserId(7L).setDeviceId(3L)
                    .setDeviceBindingId(11L).setOriginalFileName("a.jpg").setDeclaredContentType("image/jpeg")
                    .setDeclaredFileSizeBytes(200L).setReservedBytes(200L).setOriginalObjectKey("users/7/original/a.jpg")
                    .setUserTimeZoneSnapshot("Asia/Shanghai").setStatus(UploadSessionStatusEnum.URL_ISSUED);
        }

        ObjectMetadata metadata() {
            return new ObjectMetadata().setObjectKey("users/7/original/a.jpg").setSizeBytes(200L)
                    .setContentType("image/jpeg").setWidthPixels(3000).setHeightPixels(2000).setSha256("original");
        }
    }
}
