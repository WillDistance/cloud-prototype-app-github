package com.app.service.impl;

import com.app.constants.RedisKeyConstants;
import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.mapper.PhotoFileMapper;
import com.app.pojo.dto.DeletePhotosRequest;
import com.app.pojo.dto.PhotoDetailRequest;
import com.app.pojo.dto.PhotoListRequest;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.PhotoDetailVo;
import com.app.pojo.vo.PhotoDownloadVo;
import com.app.pojo.vo.PhotoPageVo;
import com.app.pojo.vo.PhotoVo;
import com.app.records.DateRange;
import com.app.service.PhotoService;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 相册照片业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoFileMapper, PhotoFileEntity> implements PhotoService {

    @Autowired
    private PhotoFileMapper photoFileMapper;

    @Autowired
    private ObsFileService obsFileService;
    @Autowired
    private ObsProperties obsProperties;
    @Autowired
    private StringRedisTemplate redis;

    @Override
    public PhotoPageVo listPhotos(PhotoListRequest request) {
        int size = request.getSize();
        DateRange range = dateRange(request.getRange());
        List<PhotoFileEntity> photos = photoFileMapper.selectAvailableByCursor(UserContextHolderUtil.getUserId(), range.startTime(), range.endTime(), request.getCursorTime(), request.getCursorId(), size + 1);
        boolean hasMore = photos.size() > size;
        if (hasMore) {
            photos = photos.subList(0, size);
        }
        PhotoFileEntity last = photos.isEmpty() ? null : photos.get(photos.size() - 1);
        return new PhotoPageVo(photos.stream().map(this::toVo).toList(), hasMore, last == null ? null : last.getUploadedTime().toString(), last == null ? null : last.getId());
    }

    @Override
    public PhotoDetailVo getPhotoDetail(PhotoDetailRequest request) {
        PhotoFileEntity photo = requireOwnedAvailable(request.getPhotoFileId());
        String previewKey = photo.getObjectKey().substring(0, photo.getObjectKey().lastIndexOf('/') + 1)
                + "preview/" + photo.getObjectKey().substring(photo.getObjectKey().lastIndexOf('/') + 1);
        String previewUrl = getDownloadUrl(previewKey);
        return new PhotoDetailVo(photo.getId(), photo.getFileName(), previewUrl, photo.getUploadedTime());
    }

    @Override
    public PhotoDownloadVo getOriginalDownloadUrl(PhotoDetailRequest request) {
        PhotoFileEntity photo = requireOwnedAvailable(request.getPhotoFileId());
        return new PhotoDownloadVo(getDownloadUrl(photo.getObjectKey()));
    }

    @Override
    public void deletePhotos(DeletePhotosRequest request) {
        for (Long photoFileId : request.getPhotoFileIds()) {
            PhotoFileEntity photo = requireOwnedAvailable(photoFileId);
            photoFileMapper.updateDeleteStatus(photo.getId(), PhotoFileStatusEnum.DELETE_PENDING.getValue(), DeleteReasonEnum.USER_MANUAL.getValue());
            evictDownloadUrl(photo.getObjectKey());
        }
    }

    /**
     * 根据用户时区计算相册筛选的UTC左闭右开时间范围。
     *
     * @param range 时间筛选范围
     * @return UTC起止时间
     */
    private DateRange dateRange(String range) {
        if (PhotoRangeEnum.ALL.getValue().equalsIgnoreCase(range)) {
            return new DateRange(null, null);
        }
        ZoneId zone = UserContextHolderUtil.getZoneId();
        LocalDate today = LocalDate.now(zone);
        LocalDate start = switch (range.toUpperCase()) {
            case "TODAY" -> today;
            case "SEVEN_DAYS" -> today.minusDays(6);
            case "ONE_MONTH" -> today.minusMonths(1);
            default -> throw new BusinessException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        };
        return new DateRange(start.atStartOfDay(zone).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime(), today.plusDays(1).atStartOfDay(zone).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());
    }

    /**
     * 查询并校验照片属于当前用户且已经发布。
     *
     * @param photoFileId 照片文件记录主键ID
     * @return 当前用户可访问的照片实体
     */
    private PhotoFileEntity requireOwnedAvailable(Long photoFileId) {
        PhotoFileEntity photo = photoFileMapper.selectById(photoFileId);
        if (photo == null || !photo.getUserId().equals(UserContextHolderUtil.getUserId()) || !PhotoFileStatusEnum.AVAILABLE.getValue().equals(photo.getStatus()) || !PhotoFileTypeEnum.ORIGINAL.getValue().equals(photo.getFileType())) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_UNAVAILABLE);
        }
        return photo;
    }

    /**
     * 删除照片原图对应的下载地址缓存。
     *
     * @param objectKey 原图对象路径
     */
    private void evictDownloadUrl(String objectKey) {
        String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
        String baseDir = objectKey.substring(0, objectKey.lastIndexOf('/') + 1);
        List<String> objectKeys = List.of(objectKey, baseDir + "thumbnail/" + fileName, baseDir + "preview/" + fileName);
        for (String key : objectKeys) {
            redis.delete(RedisKeyConstants.OBJECT_DOWNLOAD_URL_PREFIX + key);
        }
    }


    /**
     * 从Redis读取或生成对象存储下载预签名地址。
     *
     * @param objectKey 对象存储路径
     * @return 三天有效的对象存储下载地址
     */
    private String getDownloadUrl(String objectKey) {
        String key = RedisKeyConstants.OBJECT_DOWNLOAD_URL_PREFIX + objectKey;
        String cached = redis.opsForValue().get(key);
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        String url = obsFileService.generatePresignedDownloadUrl(obsProperties.getBucketName(), objectKey, obsProperties.getDownloadUrlTtl());
        redis.opsForValue().set(key, url, obsProperties.getDownloadUrlTtl());
        return url;
    }

    /**
     * 生成照片缩略图对象的预签名访问地址。
     *
     * @param photo 原图文件记录
     * @return 缩略图预签名访问地址
     */
    private String thumbnailUrl(PhotoFileEntity photo) {
        String objectKey = photo.getObjectKey();
        String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
        String baseKey = objectKey.substring(0, objectKey.lastIndexOf('/') + 1);
        return getDownloadUrl(baseKey + "thumbnail/" + fileName);
    }

    /**
     * 将照片文件实体转换为列表响应。
     *
     * @param photo 照片文件实体
     * @return 相册照片响应
     */
    private PhotoVo toVo(PhotoFileEntity photo) {
        return new PhotoVo(photo.getId(), photo.getFileName(), photo.getFileType(), thumbnailUrl(photo), photo.getUploadedTime());
    }
}
