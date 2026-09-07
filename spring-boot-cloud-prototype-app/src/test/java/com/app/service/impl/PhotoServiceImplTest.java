package com.app.service.impl;

import com.app.enums.DeleteReasonEnum;
import com.app.enums.ErrorCodeEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.enums.PhotoStatusEnum;
import com.app.exception.BusinessException;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.PhotoMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.CursorPageResult;
import com.app.pojo.dto.PhotoDto;
import com.app.pojo.entity.PhotoEntity;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.PhotoVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.support.objectstorage.ObjectStorageService;
import com.app.support.objectstorage.PhotoDownloadUrlSigner;
import com.app.utils.UtcTimeRange;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 相册查询服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class PhotoServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-03-08T16:30:00Z");

    static {
        MapperBuilderAssistant photoAssistant = new MapperBuilderAssistant(new org.apache.ibatis.session.Configuration(), "photo-test");
        photoAssistant.setCurrentNamespace("com.app.mapper.PhotoMapper");
        TableInfoHelper.initTableInfo(photoAssistant, PhotoEntity.class);
        MapperBuilderAssistant fileAssistant = new MapperBuilderAssistant(new org.apache.ibatis.session.Configuration(), "photo-file-test");
        fileAssistant.setCurrentNamespace("com.app.mapper.PhotoFileMapper");
        TableInfoHelper.initTableInfo(fileAssistant, PhotoFileEntity.class);
    }

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void shouldCalculateNaturalRangesWithJwtTimeZoneIncludingDst() {
        PhotoServiceImpl service = service(mock(PhotoMapper.class), mock(PhotoFileMapper.class), mock(PhotoDownloadUrlSigner.class));
        UtcTimeRange today = range(service, "TODAY", "America/New_York");
        UtcTimeRange sevenDays = range(service, "SEVEN_DAYS", "America/New_York");
        UtcTimeRange month = range(service, "ONE_MONTH", "America/New_York");

        assertEquals(Instant.parse("2026-03-08T05:00:00Z"), today.getStartUtc());
        assertEquals(Instant.parse("2026-03-09T04:00:00Z"), today.getEndUtc());
        assertEquals(Duration.ofHours(23), today.duration());
        assertEquals(Instant.parse("2026-03-02T05:00:00Z"), sevenDays.getStartUtc());
        assertEquals(Instant.parse("2026-02-08T05:00:00Z"), month.getStartUtc());
        assertEquals(null, range(service, "ALL", "America/New_York"));

        PhotoServiceImpl fallService = new PhotoServiceImpl(mock(PhotoMapper.class), mock(PhotoFileMapper.class),
                mock(PhotoDownloadUrlSigner.class), Clock.fixed(Instant.parse("2026-11-01T17:00:00Z"), ZoneOffset.UTC));
        assertEquals(Duration.ofHours(25), range(fallService, "TODAY", "America/New_York").duration());
    }

    @Test
    void shouldUseStableUploadedTimeAndIdCursorAndReturnOnlyThumbnailUrls() {
        PhotoMapper photoMapper = mock(PhotoMapper.class);
        PhotoFileMapper fileMapper = mock(PhotoFileMapper.class);
        PhotoDownloadUrlSigner signer = mock(PhotoDownloadUrlSigner.class);
        LocalDateTime sameTime = LocalDateTime.of(2026, 3, 8, 15, 0);
        PhotoEntity first = photo(10L, "PHO-10", sameTime);
        PhotoEntity second = photo(9L, "PHO-9", sameTime);
        when(photoMapper.selectList(any())).thenReturn(List.of(first, second));
        when(fileMapper.selectOne(any())).thenReturn(new PhotoFileEntity().setPhotoId(10L)
                .setFileType(PhotoFileTypeEnum.THUMBNAIL).setStatus(PhotoStatusEnum.AVAILABLE).setObjectKey("thumb/key"));
        when(signer.createGetUrl(any(), any())).thenReturn("https://oss.example/thumb");
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "jwt"));

        CursorPageResult<PhotoVo.ListItem> result = service(photoMapper, fileMapper, signer).listPhotos(
                new PhotoDto.ListQuery().setFilter("ALL").setSize(1)
                        .setCursorTime(Instant.parse("2026-03-08T15:30:00Z")).setCursorId(11L));

        assertTrue(result.isHasMore());
        assertEquals(10L, result.getNextCursorId());
        assertEquals("https://oss.example/thumb", result.getItems().getFirst().getThumbnailUrl());
        ArgumentCaptor<LambdaQueryWrapper<PhotoEntity>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(photoMapper).selectList(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id") && sql.contains("status") && sql.contains("uploaded_time") && sql.contains("id"));
        assertTrue(sql.contains("ORDER BY uploaded_time DESC,id DESC LIMIT 2"));
    }

    @Test
    void shouldIsolateUsersRejectUnavailableAndNeverFallbackWhenOriginalMissing() {
        PhotoMapper photoMapper = mock(PhotoMapper.class);
        PhotoFileMapper fileMapper = mock(PhotoFileMapper.class);
        PhotoDownloadUrlSigner signer = mock(PhotoDownloadUrlSigner.class);
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "jwt"));
        PhotoServiceImpl service = service(photoMapper, fileMapper, signer);
        when(photoMapper.selectOne(any())).thenReturn(photo(3L, "PHO-3", LocalDateTime.of(2026, 3, 8, 1, 0)));
        when(fileMapper.selectOne(any())).thenReturn(null);

        BusinessException missingOriginal = assertThrows(BusinessException.class,
                () -> service.getOriginalDownloadUrl("PHO-3"));
        assertEquals(ErrorCodeEnum.PHOTO_UNAVAILABLE, missingOriginal.getErrorCode());
        verify(signer, never()).createGetUrl(any(), any());

        ArgumentCaptor<LambdaQueryWrapper<PhotoEntity>> ownerCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(photoMapper).selectOne(ownerCaptor.capture());
        String ownerSql = ownerCaptor.getValue().getSqlSegment();
        assertTrue(ownerSql.contains("user_id") && ownerSql.contains("photo_no") && ownerSql.contains("status"));

        when(photoMapper.selectOne(any())).thenReturn(null);
        BusinessException otherUser = assertThrows(BusinessException.class, () -> service.getPhotoDetail("OTHER"));
        assertEquals(ErrorCodeEnum.PHOTO_NOT_FOUND, otherUser.getErrorCode());
    }

    @Test
    void shouldDeletePhotoGroupAndDecreaseOriginalBytesOnce() {
        PhotoMapper photoMapper = mock(PhotoMapper.class);
        PhotoFileMapper fileMapper = mock(PhotoFileMapper.class);
        UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        ObjectStorageService storage = mock(ObjectStorageService.class);
        PhotoEntity photo = photo(1L, "PHO-1", LocalDateTime.now()).setOriginalSizeBytes(123L);
        when(photoMapper.selectList(any())).thenReturn(List.of(photo));
        when(fileMapper.selectList(any())).thenReturn(List.of(file(1L, "original"), file(1L, "thumb"), file(1L, "preview")));
        when(photoMapper.updateById(org.mockito.ArgumentMatchers.<PhotoEntity>any())).thenReturn(1);
                when(fileMapper.updateById(org.mockito.ArgumentMatchers.<PhotoFileEntity>any())).thenReturn(1);
        when(accountMapper.decreaseUsedBytes(7L, 123L)).thenReturn(1);
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "jwt"));

        deleteService(photoMapper, fileMapper, accountMapper, storage)
                .deletePhotos(new PhotoDto.DeleteRequest().setPhotoNos(List.of("PHO-1")));

        verify(storage).delete("original");
        verify(storage).delete("thumb");
        verify(storage).delete("preview");
        verify(accountMapper).decreaseUsedBytes(7L, 123L);
    }

    @Test
    void shouldRejectOverNineDuplicateAndOtherUserPhotos() {
        PhotoMapper photoMapper = mock(PhotoMapper.class);
        PhotoServiceImpl service = deleteService(photoMapper, mock(PhotoFileMapper.class),
                mock(UserStorageAccountMapper.class), mock(ObjectStorageService.class));
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "jwt"));

        assertThrows(com.app.exception.RequestParameterException.class, () -> service.deletePhotos(
                new PhotoDto.DeleteRequest().setPhotoNos(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))));
        assertThrows(com.app.exception.RequestParameterException.class, () -> service.deletePhotos(
                new PhotoDto.DeleteRequest().setPhotoNos(List.of("PHO-1", "PHO-1"))));
        when(photoMapper.selectList(any())).thenReturn(List.of());
        BusinessException otherUser = assertThrows(BusinessException.class, () -> service.deletePhotos(
                new PhotoDto.DeleteRequest().setPhotoNos(List.of("OTHER"))));
        assertEquals(ErrorCodeEnum.PHOTO_NOT_FOUND, otherUser.getErrorCode());
        verify(photoMapper, never()).updateById(org.mockito.ArgumentMatchers.<PhotoEntity>any());
    }

    @Test
    void shouldAcceptNinePhotos() {
        PhotoMapper photoMapper = mock(PhotoMapper.class);
        PhotoFileMapper fileMapper = mock(PhotoFileMapper.class);
        UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        List<String> photoNos = java.util.stream.LongStream.rangeClosed(1, 9)
                .mapToObj(value -> "PHO-" + value).toList();
        List<PhotoEntity> photos = java.util.stream.LongStream.rangeClosed(1, 9)
                .mapToObj(value -> photo(value, "PHO-" + value, LocalDateTime.now()).setOriginalSizeBytes(10L)).toList();
        when(photoMapper.selectList(any())).thenReturn(photos);
        when(fileMapper.selectList(any())).thenAnswer(invocation -> List.of(
                file(1L, "original"), file(1L, "thumb"), file(1L, "preview")));
        when(photoMapper.updateById(org.mockito.ArgumentMatchers.<PhotoEntity>any())).thenReturn(1);
        when(fileMapper.updateById(org.mockito.ArgumentMatchers.<PhotoFileEntity>any())).thenReturn(1);
        when(accountMapper.decreaseUsedBytes(7L, 10L)).thenReturn(1);
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "jwt"));

        deleteService(photoMapper, fileMapper, accountMapper, mock(ObjectStorageService.class))
                .deletePhotos(new PhotoDto.DeleteRequest().setPhotoNos(photoNos));

        verify(accountMapper, times(9)).decreaseUsedBytes(7L, 10L);
    }

    @Test
    void shouldRetryOnlyFailedOssFileAndNeverDecreaseUsageTwice() {
        PhotoMapper photoMapper = mock(PhotoMapper.class);
        PhotoFileMapper fileMapper = mock(PhotoFileMapper.class);
        UserStorageAccountMapper accountMapper = mock(UserStorageAccountMapper.class);
        ObjectStorageService storage = mock(ObjectStorageService.class);
        PhotoEntity photo = photo(1L, "PHO-1", LocalDateTime.now()).setOriginalSizeBytes(123L);
        PhotoFileEntity preview = file(1L, "preview");
        when(photoMapper.selectList(any())).thenReturn(List.of(photo));
        when(fileMapper.selectList(any())).thenReturn(
                List.of(file(1L, "original"), file(1L, "thumb"), preview), List.of(preview), List.of());
        when(photoMapper.updateById(org.mockito.ArgumentMatchers.<PhotoEntity>any())).thenReturn(1);
                when(fileMapper.updateById(org.mockito.ArgumentMatchers.<PhotoFileEntity>any())).thenReturn(1);
        when(accountMapper.decreaseUsedBytes(7L, 123L)).thenReturn(1);
        doThrow(new RuntimeException("OSS unavailable")).doNothing().when(storage).delete("preview");
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "jwt"));
        PhotoServiceImpl service = deleteService(photoMapper, fileMapper, accountMapper, storage);
        PhotoDto.DeleteRequest request = new PhotoDto.DeleteRequest().setPhotoNos(List.of("PHO-1"));

        service.deletePhotos(request);
        assertEquals(PhotoStatusEnum.DELETE_FAILED, photo.getStatus());
        assertEquals(PhotoStatusEnum.DELETE_FAILED, preview.getStatus());
        assertEquals(DeleteReasonEnum.USER_MANUAL, preview.getDeleteReason());
        verify(accountMapper, never()).decreaseUsedBytes(any(), any());

        service.deletePhotos(request);
        service.deletePhotos(request);
        verify(storage, times(2)).delete("preview");
        verify(storage, times(1)).delete("original");
        verify(accountMapper, times(1)).decreaseUsedBytes(7L, 123L);
    }

    @SuppressWarnings("unchecked")
    private UtcTimeRange range(PhotoServiceImpl service, String filter, String zone) {
        return (UtcTimeRange) ReflectionTestUtils.invokeMethod(service, "timeRange", filter, zone);
    }

    private PhotoEntity photo(long id, String photoNo, LocalDateTime uploadedTime) {
        return new PhotoEntity().setId(id).setPhotoNo(photoNo).setUserId(7L).setStatus(PhotoStatusEnum.AVAILABLE)
                .setFileName(photoNo + ".jpg").setUploadedTime(uploadedTime);
    }

    private PhotoFileEntity file(long photoId, String key) {
        PhotoFileTypeEnum type = switch (key) {
            case "original" -> PhotoFileTypeEnum.ORIGINAL;
            case "thumb" -> PhotoFileTypeEnum.THUMBNAIL;
            default -> PhotoFileTypeEnum.PREVIEW;
        };
        return new PhotoFileEntity().setId((long) key.hashCode() & 0x7fffffffL).setPhotoId(photoId)
                .setFileType(type).setObjectKey(key).setStatus(PhotoStatusEnum.AVAILABLE);
    }

    private PhotoServiceImpl deleteService(PhotoMapper photoMapper, PhotoFileMapper fileMapper,
                                           UserStorageAccountMapper accountMapper, ObjectStorageService storage) {
        return new PhotoServiceImpl(photoMapper, fileMapper, mock(PhotoDownloadUrlSigner.class), accountMapper, storage, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private PhotoServiceImpl service(PhotoMapper photoMapper, PhotoFileMapper fileMapper, PhotoDownloadUrlSigner signer) {
        return new PhotoServiceImpl(photoMapper, fileMapper, signer, Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
