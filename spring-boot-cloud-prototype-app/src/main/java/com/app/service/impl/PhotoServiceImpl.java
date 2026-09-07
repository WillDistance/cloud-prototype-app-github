package com.app.service.impl;

import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.exception.RequestParameterException;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.PhotoMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.CursorPageRequest;
import com.app.pojo.CursorPageResult;
import com.app.pojo.dto.PhotoDto;
import com.app.pojo.entity.PhotoEntity;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.PhotoVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.PhotoService;
import com.app.support.objectstorage.ObjectStorageService;
import com.app.support.objectstorage.PhotoDownloadUrlSigner;
import com.app.utils.UserTimeZoneUtil;
import com.app.utils.UtcTimeRange;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 当前用户相册查询服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class PhotoServiceImpl implements PhotoService {
    private static final int DOWNLOAD_URL_TTL_SECONDS = 300;
    private final PhotoMapper photoMapper;
    private final PhotoFileMapper photoFileMapper;
    private final PhotoDownloadUrlSigner downloadUrlSigner;
    private final UserStorageAccountMapper accountMapper;
    private final ObjectStorageService storageService;
    private final Clock clock;

    @Autowired
    public PhotoServiceImpl(PhotoMapper photoMapper, PhotoFileMapper photoFileMapper,
                            PhotoDownloadUrlSigner downloadUrlSigner, UserStorageAccountMapper accountMapper,
                            ObjectStorageService storageService) {
        this(photoMapper, photoFileMapper, downloadUrlSigner, accountMapper, storageService, Clock.systemUTC());
    }

    PhotoServiceImpl(PhotoMapper photoMapper, PhotoFileMapper photoFileMapper,
                     PhotoDownloadUrlSigner downloadUrlSigner, Clock clock) {
        this(photoMapper, photoFileMapper, downloadUrlSigner, null, null, clock);
    }

    PhotoServiceImpl(PhotoMapper photoMapper, PhotoFileMapper photoFileMapper,
                     PhotoDownloadUrlSigner downloadUrlSigner, UserStorageAccountMapper accountMapper,
                     ObjectStorageService storageService, Clock clock) {
        this.photoMapper = photoMapper;
        this.photoFileMapper = photoFileMapper;
        this.downloadUrlSigner = downloadUrlSigner;
        this.accountMapper = accountMapper;
        this.storageService = storageService;
        this.clock = clock;
    }

    @Override
    public CursorPageResult<PhotoVo.ListItem> listPhotos(PhotoDto.ListQuery query) {
        AuthenticatedUser currentUser = requireCurrentUser();
        PhotoDto.ListQuery normalized = query == null ? new PhotoDto.ListQuery() : query;
        CursorPageRequest page = pageRequest(normalized);
        UtcTimeRange range = timeRange(normalized.getFilter(), currentUser.timeZone());
        LambdaQueryWrapper<PhotoEntity> wrapper = availablePhotos(currentUser.userId());
        if (range != null) {
            wrapper.ge(PhotoEntity::getUploadedTime, LocalDateTime.ofInstant(range.getStartUtc(), ZoneOffset.UTC))
                    .lt(PhotoEntity::getUploadedTime, LocalDateTime.ofInstant(range.getEndUtc(), ZoneOffset.UTC));
        }
        if (page.hasCursor()) {
            LocalDateTime cursorTime = LocalDateTime.ofInstant(page.getCursorTime(), ZoneOffset.UTC);
            wrapper.and(w -> w.lt(PhotoEntity::getUploadedTime, cursorTime)
                    .or().eq(PhotoEntity::getUploadedTime, cursorTime).lt(PhotoEntity::getId, page.getCursorId()));
        }
        wrapper.orderByDesc(PhotoEntity::getUploadedTime).orderByDesc(PhotoEntity::getId).last("LIMIT " + (page.getSize() + 1));
        List<PhotoEntity> fetched = photoMapper.selectList(wrapper);
        boolean hasMore = fetched.size() > page.getSize();
        List<PhotoEntity> pageRows = hasMore ? fetched.subList(0, page.getSize()) : fetched;
        List<PhotoVo.ListItem> items = new ArrayList<>();
        for (PhotoEntity photo : pageRows) {
            PhotoFileEntity thumbnail = findAvailableFile(photo.getId(), PhotoFileTypeEnum.THUMBNAIL);
            if (thumbnail != null) {
                items.add(toListItem(photo, thumbnail));
            }
        }
        if (!hasMore) {
            return CursorPageResult.lastPage(items);
        }
        PhotoEntity cursor = pageRows.get(pageRows.size() - 1);
        return CursorPageResult.of(items, true, cursor.getUploadedTime().toInstant(ZoneOffset.UTC), cursor.getId());
    }

    @Override
    public PhotoVo.Detail getPhotoDetail(String photoNo) {
        PhotoEntity photo = requireOwnedAvailablePhoto(photoNo, requireCurrentUser().userId());
        PhotoFileEntity preview = findAvailableFile(photo.getId(), PhotoFileTypeEnum.PREVIEW);
        if (preview == null) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_UNAVAILABLE);
        }
        return new PhotoVo.Detail().setPhotoNo(photo.getPhotoNo()).setFileName(photo.getFileName())
                .setOriginalMimeType(photo.getOriginalMimeType()).setOriginalSizeBytes(photo.getOriginalSizeBytes())
                .setWidthPixels(photo.getWidthPixels()).setHeightPixels(photo.getHeightPixels())
                .setTakenTime(photo.getTakenTime()).setUploadedTime(photo.getUploadedTime())
                .setPreviewUrl(sign(preview.getObjectKey()));
    }

    @Override
    public PhotoVo.DownloadUrl getOriginalDownloadUrl(String photoNo) {
        PhotoEntity photo = requireOwnedAvailablePhoto(photoNo, requireCurrentUser().userId());
        PhotoFileEntity original = findAvailableFile(photo.getId(), PhotoFileTypeEnum.ORIGINAL);
        if (original == null) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_UNAVAILABLE);
        }
        return new PhotoVo.DownloadUrl().setDownloadUrl(sign(original.getObjectKey()));
    }

    @Override
    @Transactional
    public void deletePhotos(PhotoDto.DeleteRequest request) {
        AuthenticatedUser currentUser = requireCurrentUser();
        List<String> photoNos = request == null ? null : request.getPhotoNos();
        if (photoNos == null || photoNos.isEmpty() || photoNos.size() > 9
                || photoNos.stream().anyMatch(value -> value == null || value.isBlank())
                || new HashSet<>(photoNos).size() != photoNos.size()) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        List<PhotoEntity> photos = photoMapper.selectList(new LambdaQueryWrapper<PhotoEntity>()
                .eq(PhotoEntity::getUserId, currentUser.userId()).in(PhotoEntity::getPhotoNo, photoNos)
                .in(PhotoEntity::getStatus, PhotoStatusEnum.AVAILABLE, PhotoStatusEnum.DELETE_PENDING,
                        PhotoStatusEnum.DELETE_FAILED));
        if (photos.size() != photoNos.size()) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_NOT_FOUND);
        }
        List<List<PhotoFileEntity>> fileGroups = new ArrayList<>();
        for (PhotoEntity photo : photos) {
            List<PhotoFileEntity> files = photoFileMapper.selectList(new LambdaQueryWrapper<PhotoFileEntity>()
                    .eq(PhotoFileEntity::getPhotoId, photo.getId()));
            if (photo.getStatus() == PhotoStatusEnum.AVAILABLE && !hasCompleteFileGroup(files)) {
                throw new BusinessException(ErrorCodeEnum.PHOTO_UNAVAILABLE);
            }
            fileGroups.add(files);
        }
        for (int index = 0; index < photos.size(); index++) {
            deletePhotoGroup(photos.get(index), fileGroups.get(index), currentUser.userId());
        }
    }

    /**
     * 删除照片及其文件，并更新用户存储使用量。
     *
     * @param photo  照片记录
     * @param files  照片文件列表
     * @param userId 用户ID
     */
    private void deletePhotoGroup(PhotoEntity photo, List<PhotoFileEntity> files, Long userId) {
        if (files.isEmpty() && photo.getStatus() != PhotoStatusEnum.AVAILABLE) {
            return;
        }
        photo.setStatus(PhotoStatusEnum.DELETE_PENDING);
        if (photoMapper.updateById(photo) != 1) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        }
        for (PhotoFileEntity file : files) {
            file.setStatus(PhotoStatusEnum.DELETE_PENDING).setDeleteReason(DeleteReasonEnum.USER_MANUAL);
            if (photoFileMapper.updateById(file) != 1) {
                throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
            }
        }

        boolean failed = false;
        for (PhotoFileEntity file : files) {
            try {
                storageService.delete(file.getObjectKey());
                photoFileMapper.deleteById(file.getId());
            } catch (RuntimeException exception) {
                failed = true;
                file.setStatus(PhotoStatusEnum.DELETE_FAILED).setDeleteReason(DeleteReasonEnum.USER_MANUAL);
                photoFileMapper.updateById(file);
            }
        }
        if (failed) {
            photo.setStatus(PhotoStatusEnum.DELETE_FAILED);
            photoMapper.updateById(photo);
            return;
        }
        long originalBytes = photo.getOriginalSizeBytes() == null ? 0L : photo.getOriginalSizeBytes();
        if (originalBytes > 0 && accountMapper.decreaseUsedBytes(userId, originalBytes) != 1) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        }
    }

    /**
     * 判断照片文件组是否包含完整的可用文件。
     *
     * @param files 照片文件列表
     * @return 操作是否成功
     */
    private boolean hasCompleteFileGroup(List<PhotoFileEntity> files) {
        Set<PhotoFileTypeEnum> types = new HashSet<>();
        for (PhotoFileEntity file : files) {
            types.add(file.getFileType());
        }
        return types.containsAll(List.of(PhotoFileTypeEnum.ORIGINAL, PhotoFileTypeEnum.THUMBNAIL,
                PhotoFileTypeEnum.PREVIEW));
    }

    /**
     * 将照片实体和缩略图文件转换为相册列表项。
     *
     * @param photo     照片记录
     * @param thumbnail 照片缩略图文件记录
     * @return 方法处理后的结果
     */
    private PhotoVo.ListItem toListItem(PhotoEntity photo, PhotoFileEntity thumbnail) {
        return new PhotoVo.ListItem().setPhotoNo(photo.getPhotoNo()).setFileName(photo.getFileName())
                .setWidthPixels(photo.getWidthPixels()).setHeightPixels(photo.getHeightPixels()).setTakenTime(photo.getTakenTime())
                .setUploadedTime(photo.getUploadedTime()).setThumbnailUrl(sign(thumbnail.getObjectKey()));
    }

    /**
     * 为对象存储文件生成下载地址。
     *
     * @param objectKey 对象存储文件键
     * @return 方法处理后的结果
     */
    private String sign(String objectKey) {
        return downloadUrlSigner.createGetUrl(objectKey, LocalDateTime.ofInstant(Instant.now(clock)
                .plusSeconds(DOWNLOAD_URL_TTL_SECONDS), ZoneOffset.UTC));
    }

    /**
     * 校验照片属于当前用户且处于可用状态。
     *
     * @param photoNo 照片编号
     * @param userId  用户ID
     * @return 方法处理后的结果
     */
    private PhotoEntity requireOwnedAvailablePhoto(String photoNo, Long userId) {
        if (photoNo == null || photoNo.isBlank()) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        PhotoEntity photo = photoMapper.selectOne(availablePhotos(userId).eq(PhotoEntity::getPhotoNo, photoNo));
        if (photo == null) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_NOT_FOUND);
        }
        return photo;
    }

    /**
     * 查询指定照片类型的可用文件。
     *
     * @param photoId 照片ID
     * @param type    文件类型
     * @return 方法处理后的结果
     */
    private PhotoFileEntity findAvailableFile(Long photoId, PhotoFileTypeEnum type) {
        return photoFileMapper.selectOne(new LambdaQueryWrapper<PhotoFileEntity>().eq(PhotoFileEntity::getPhotoId, photoId)
                .eq(PhotoFileEntity::getFileType, type).eq(PhotoFileEntity::getStatus, PhotoStatusEnum.AVAILABLE));
    }

    /**
     * 构造当前用户可用照片的查询条件。
     *
     * @param userId 用户ID
     * @return 方法处理后的结果
     */
    private LambdaQueryWrapper<PhotoEntity> availablePhotos(Long userId) {
        return new LambdaQueryWrapper<PhotoEntity>().eq(PhotoEntity::getUserId, userId)
                .eq(PhotoEntity::getStatus, PhotoStatusEnum.AVAILABLE);
    }

    /**
     * 将照片列表查询参数转换为游标分页请求。
     *
     * @param query 照片查询条件
     * @return 方法处理后的结果
     */
    private CursorPageRequest pageRequest(PhotoDto.ListQuery query) {
        try {
            int size = query.getSize() == null ? 20 : query.getSize();
            if ((query.getCursorTime() == null) != (query.getCursorId() == null)) {
                throw new IllegalArgumentException("游标时间和游标ID必须同时提供");
            }
            return query.getCursorTime() == null ? CursorPageRequest.firstPage(size)
                    : CursorPageRequest.after(query.getCursorTime(), query.getCursorId(), size);
        } catch (IllegalArgumentException exception) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
    }

    /**
     * 按用户时区将日期筛选条件转换为UTC时间范围。
     *
     * @param value    权益时长数值
     * @param timeZone 用户时区
     * @return 方法处理后的结果
     */
    private UtcTimeRange timeRange(String value, String timeZone) {
        PhotoTimeFilterEnum filter;
        try {
            filter = PhotoTimeFilterEnum.valueOf(value == null ? "ALL" : value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        ZoneId zone = UserTimeZoneUtil.requireIanaZone(timeZone);
        LocalDate today = Instant.now(clock).atZone(zone).toLocalDate();
        return switch (filter) {
            case ALL -> null;
            case TODAY -> UserTimeZoneUtil.toUtcDayRange(today, timeZone);
            case SEVEN_DAYS -> localDateRange(today.minusDays(6), today.plusDays(1), timeZone);
            case ONE_MONTH -> localDateRange(today.minusMonths(1), today.plusDays(1), timeZone);
        };
    }

    /**
     * 按用户时区将本地日期范围转换为UTC时间范围。
     *
     * @param start        开始日期
     * @param endExclusive 结束日期，不包含当天
     * @param timeZone     用户时区
     * @return 方法处理后的结果
     */
    private UtcTimeRange localDateRange(LocalDate start, LocalDate endExclusive, String timeZone) {
        return new UtcTimeRange(start.atStartOfDay(UserTimeZoneUtil.requireIanaZone(timeZone)).toInstant(),
                endExclusive.atStartOfDay(UserTimeZoneUtil.requireIanaZone(timeZone)).toInstant());
    }

    /**
     * 获取当前认证用户，不存在时抛出认证异常。
     *
     * @return 方法处理后的结果
     */
    private AuthenticatedUser requireCurrentUser() {
        AuthenticatedUser user = UserContextHolder.get();
        if (user == null || user.userId() == null || user.timeZone() == null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_REQUIRED);
        }
        return user;
    }
}
