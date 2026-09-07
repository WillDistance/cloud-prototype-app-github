package com.app.service.impl;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.enums.PhotoStatusEnum;
import com.app.enums.UploadSessionStatusEnum;
import com.app.exception.BusinessException;
import com.app.exception.OssCallbackRejectedException;
import com.app.exception.PhotoProcessingRetryableException;
import com.app.mapper.*;
import com.app.pojo.dto.OssCallbackDto;
import com.app.pojo.entity.PhotoEntity;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.entity.PlatformConfigEntity;
import com.app.pojo.entity.UploadSessionEntity;
import com.app.pojo.vo.OssCallbackVo;
import com.app.service.OssCallbackService;
import com.app.support.objectstorage.ObjectMetadata;
import com.app.support.objectstorage.ObjectStorageService;
import com.app.support.oss.OssCallbackVerifier;
import com.app.support.photo.ImageProcessingResult;
import com.app.support.photo.PhotoImageProcessor;
import com.app.utils.BusinessNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * OSS回调与照片发布服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class OssCallbackServiceImpl implements OssCallbackService {
    private final UploadSessionMapper sessionMapper;
    private final PhotoMapper photoMapper;
    private final PhotoFileMapper photoFileMapper;
    private final UserStorageAccountMapper accountMapper;
    private final PlatformConfigMapper configMapper;
    private final OssCallbackVerifier callbackVerifier;
    private final ObjectStorageService storageService;
    private final PhotoImageProcessor imageProcessor;
    private final Supplier<LocalDateTime> nowSupplier;
    private final BusinessNumberGenerator numberGenerator = new BusinessNumberGenerator();

    @Autowired
    public OssCallbackServiceImpl(UploadSessionMapper sessionMapper, PhotoMapper photoMapper,
                                  PhotoFileMapper photoFileMapper, UserStorageAccountMapper accountMapper,
                                  PlatformConfigMapper configMapper, OssCallbackVerifier callbackVerifier,
                                  ObjectStorageService storageService, PhotoImageProcessor imageProcessor) {
        this(sessionMapper, photoMapper, photoFileMapper, accountMapper, configMapper, callbackVerifier,
                storageService, imageProcessor, () -> LocalDateTime.now(ZoneOffset.UTC));
    }

    public OssCallbackServiceImpl(UploadSessionMapper sessionMapper, PhotoMapper photoMapper,
                                  PhotoFileMapper photoFileMapper, UserStorageAccountMapper accountMapper,
                                  PlatformConfigMapper configMapper, OssCallbackVerifier callbackVerifier,
                                  ObjectStorageService storageService, PhotoImageProcessor imageProcessor,
                                  Supplier<LocalDateTime> nowSupplier) {
        this.sessionMapper = sessionMapper;
        this.photoMapper = photoMapper;
        this.photoFileMapper = photoFileMapper;
        this.accountMapper = accountMapper;
        this.configMapper = configMapper;
        this.callbackVerifier = callbackVerifier;
        this.storageService = storageService;
        this.imageProcessor = imageProcessor;
        this.nowSupplier = nowSupplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = {
            OssCallbackRejectedException.class, PhotoProcessingRetryableException.class
    })
    public OssCallbackVo uploadCompleted(OssCallbackDto dto) {
        validateBasic(dto);
        if (!callbackVerifier.verify(dto)) {
            throw new OssCallbackRejectedException(ErrorCodeEnum.AUTH_ACCESS_DENIED, "OSS回调签名无效");
        }
        UploadSessionEntity session = sessionMapper.selectByUploadNoForUpdate(dto.getUploadNo());
        if (session == null) throw new BusinessException(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID);
        if (session.getStatus() == UploadSessionStatusEnum.COMPLETED) return completedResult(session, true);
        if (session.getStatus() == UploadSessionStatusEnum.PROCESSING) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        }
        if (!Objects.equals(session.getOriginalObjectKey(), dto.getObjectKey())) {
            failAndRelease(session, dto.getObjectKey(), "OBJECT_KEY_MISMATCH", "对象Key与上传会话不匹配");
            throw new OssCallbackRejectedException(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID);
        }
        if (session.getStatus() != UploadSessionStatusEnum.URL_ISSUED &&
                !(session.getStatus() == UploadSessionStatusEnum.FAILED &&
                        "IMAGE_PROCESSING_FAILED".equals(session.getFailureCode()))) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID);
        }

        ObjectMetadata actual = storageService.stat(dto.getObjectKey());
        validateMetadata(session, dto, actual);
        if (session.getStatus() == UploadSessionStatusEnum.URL_ISSUED &&
                sessionMapper.markOriginalUploadedIfUrlIssued(session.getId(), dto.getEventId()) != 1) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        }
        if (sessionMapper.markProcessingIfUploadedOrRetryableFailed(session.getId()) != 1) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        }

        String thumbnailKey = derivedKey(session.getOriginalObjectKey(), "thumbnail");
        String previewKey = derivedKey(session.getOriginalObjectKey(), "preview");
        ImageProcessingResult images;
        try {
            images = imageProcessor.process(session.getOriginalObjectKey(), thumbnailKey, previewKey);
            validateProcessed(images, thumbnailKey, previewKey);
        } catch (Exception exception) {
            deleteQuietly(thumbnailKey);
            deleteQuietly(previewKey);
            String reason = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            sessionMapper.markRetryableFailedIfProcessing(session.getId(), "IMAGE_PROCESSING_FAILED", truncate(reason));
            throw new PhotoProcessingRetryableException(ErrorCodeEnum.PHOTO_PROCESSING_FAILED, "照片派生处理失败");
        }
        return publish(session, dto.getEventId(), images);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupOrphanObjects(LocalDateTime now, int batchSize) {
        if (batchSize <= 0) return 0;
        int cleaned = 0;
        for (UploadSessionEntity session : sessionMapper.selectFailedForCleanup(now, batchSize)) {
            deleteQuietly(session.getOriginalObjectKey());
            deleteQuietly(derivedKey(session.getOriginalObjectKey(), "thumbnail"));
            deleteQuietly(derivedKey(session.getOriginalObjectKey(), "preview"));
            if (sessionMapper.markCleanupReleasedIfFailed(session.getId()) == 1) {
                if (session.getReservedBytes() != null && session.getReservedBytes() > 0) {
                    accountMapper.selectByUserIdForUpdate(session.getUserId());
                    if (accountMapper.decreaseReservedBytes(session.getUserId(), session.getReservedBytes()) != 1) {
                        throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
                    }
                }
                cleaned++;
            }
        }
        return cleaned;
    }

    /**
     * 发布已处理完成的照片及其文件记录。
     *
     * @param session 上传会话记录
     * @param eventId 对象存储事件ID
     * @param images  原图、缩略图和预览图的处理结果
     * @return 方法处理后的结果
     */
    private OssCallbackVo publish(UploadSessionEntity session, String eventId, ImageProcessingResult images) {
        ObjectMetadata original = images.original();
        LocalDateTime now = nowSupplier.get();
        PhotoEntity photo = new PhotoEntity().setPhotoNo(numberGenerator.generate("PHT"))
                .setUploadSessionId(session.getId()).setUserId(session.getUserId()).setDeviceId(session.getDeviceId())
                .setDeviceBindingId(session.getDeviceBindingId()).setFileName(session.getOriginalFileName())
                .setOriginalExtension(extensionOf(session.getOriginalFileName())).setOriginalMimeType(original.getContentType())
                .setOriginalSizeBytes(original.getSizeBytes()).setOriginalSha256(original.getSha256())
                .setWidthPixels(original.getWidthPixels()).setHeightPixels(original.getHeightPixels())
                .setUploadedTime(now).setUserTimeZoneSnapshot(session.getUserTimeZoneSnapshot()).setStatus(PhotoStatusEnum.AVAILABLE);
        if (photoMapper.insert(photo) != 1) throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        insertFile(photo.getId(), PhotoFileTypeEnum.ORIGINAL, original, eventId);
        insertFile(photo.getId(), PhotoFileTypeEnum.THUMBNAIL, images.thumbnail(), null);
        insertFile(photo.getId(), PhotoFileTypeEnum.PREVIEW, images.preview(), null);
        accountMapper.selectByUserIdForUpdate(session.getUserId());
        if (accountMapper.moveReservedToUsed(session.getUserId(), session.getReservedBytes(), original.getSizeBytes()) != 1 ||
                sessionMapper.markCompletedIfProcessing(session.getId(), now) != 1) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        }
        return new OssCallbackVo().setUploadNo(session.getUploadNo()).setStatus("COMPLETED")
                .setPhotoNo(photo.getPhotoNo()).setIdempotent(false);
    }

    /**
     * 新增照片文件记录。
     *
     * @param photoId  照片ID
     * @param type     文件类型
     * @param metadata 对象存储文件元数据
     * @param eventId  对象存储事件ID
     */
    private void insertFile(Long photoId, PhotoFileTypeEnum type, ObjectMetadata metadata, String eventId) {
        PhotoFileEntity file = new PhotoFileEntity().setPhotoId(photoId).setFileType(type)
                .setObjectKey(metadata.getObjectKey()).setMimeType(metadata.getContentType()).setSizeBytes(metadata.getSizeBytes())
                .setSha256(metadata.getSha256()).setWidthPixels(metadata.getWidthPixels()).setHeightPixels(metadata.getHeightPixels())
                .setStatus(PhotoStatusEnum.AVAILABLE).setDeleteReason(null).setOssCallbackEventId(eventId);
        if (photoFileMapper.insert(file) != 1) throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
    }

    /**
     * 构造上传完成回调的返回结果。
     *
     * @param session    上传会话记录
     * @param idempotent 是否为重复回调
     * @return 上传完成回调结果，包含上传单号、照片编号和幂等标识
     */
    private OssCallbackVo completedResult(UploadSessionEntity session, boolean idempotent) {
        PhotoEntity photo = photoMapper.selectByUploadSessionId(session.getId());
        if (photo == null) throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
        return new OssCallbackVo().setUploadNo(session.getUploadNo()).setStatus("COMPLETED")
                .setPhotoNo(photo.getPhotoNo()).setIdempotent(idempotent);
    }

    /**
     * 校验对象存储回调的基础参数。
     *
     * @param dto OSS上传完成回调请求
     */
    private void validateBasic(OssCallbackDto dto) {
        if (dto == null || blank(dto.getEventId()) || blank(dto.getUploadNo()) || blank(dto.getObjectKey()) ||
                blank(dto.getContentType()) || blank(dto.getSignature()) || blank(dto.getCallbackTime()) ||
                dto.getSizeBytes() == null || dto.getSizeBytes() <= 0) {
            throw new BusinessException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        try {
            Instant.parse(dto.getCallbackTime());
        } catch (DateTimeParseException exception) {
            throw new BusinessException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER, "回调时间格式无效");
        }
    }

    /**
     * 校验回调元数据与实际对象元数据是否一致。
     *
     * @param session 上传会话记录
     * @param dto     OSS上传完成回调请求
     * @param actual  对象存储中读取到的实际文件元数据
     */
    private void validateMetadata(UploadSessionEntity session, OssCallbackDto dto, ObjectMetadata actual) {
        PlatformConfigEntity config = configMapper.selectCurrentActive(nowSupplier.get());
        long max = config == null || config.getMaxFileSizeBytes() == null ? 0 : config.getMaxFileSizeBytes();
        boolean invalid = actual == null || !Objects.equals(actual.getObjectKey(), session.getOriginalObjectKey()) ||
                actual.getSizeBytes() == null || actual.getSizeBytes() <= 0 ||
                actual.getSizeBytes() > session.getDeclaredFileSizeBytes() || actual.getSizeBytes() > max ||
                !Objects.equals(dto.getSizeBytes(), actual.getSizeBytes()) ||
                !equalsIgnoreCase(session.getDeclaredContentType(), actual.getContentType()) ||
                !equalsIgnoreCase(dto.getContentType(), actual.getContentType());
        if (invalid) {
            failAndRelease(session, session.getOriginalObjectKey(), "FILE_METADATA_INVALID", "OSS对象元数据不符合上传会话约束");
            throw new OssCallbackRejectedException(ErrorCodeEnum.PHOTO_FILE_INVALID);
        }
    }

    /**
     * 校验照片处理结果及派生文件键。
     *
     * @param result       照片处理结果
     * @param thumbnailKey 缩略图对象存储键
     * @param previewKey   预览图对象存储键
     */
    private void validateProcessed(ImageProcessingResult result, String thumbnailKey, String previewKey) {
        if (result == null || result.original() == null || result.thumbnail() == null || result.preview() == null ||
                !Objects.equals(thumbnailKey, result.thumbnail().getObjectKey()) ||
                !Objects.equals(previewKey, result.preview().getObjectKey()) ||
                result.thumbnail().getWidthPixels() == null || result.thumbnail().getHeightPixels() == null ||
                Math.max(result.thumbnail().getWidthPixels(), result.thumbnail().getHeightPixels()) > 480 ||
                result.preview().getWidthPixels() == null || result.preview().getHeightPixels() == null ||
                Math.max(result.preview().getWidthPixels(), result.preview().getHeightPixels()) > 1920) {
            throw new IllegalStateException("派生图元数据无效");
        }
    }

    /**
     * 记录上传失败并释放已预留的存储容量。
     *
     * @param session   上传会话记录
     * @param objectKey 对象存储文件键
     * @param code      失败编码
     * @param reason    失败原因
     */
    private void failAndRelease(UploadSessionEntity session, String objectKey, String code, String reason) {
        if (sessionMapper.markFailedAndReleaseIfReserved(session.getId(), code, reason) == 1) {
            accountMapper.selectByUserIdForUpdate(session.getUserId());
            if (accountMapper.decreaseReservedBytes(session.getUserId(), session.getReservedBytes()) != 1) {
                throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
            }
        }
        deleteQuietly(objectKey);
    }

    /**
     * 根据原文件键生成派生文件键。
     *
     * @param originalKey 原文件键
     * @param folder      派生文件目录
     * @return 方法处理后的结果
     */
    private String derivedKey(String originalKey, String folder) {
        if (originalKey == null) return null;
        String marker = "/original/";
        return originalKey.contains(marker) ? originalKey.replace(marker, "/" + folder + "/") : folder + "/" + originalKey;
    }

    /**
     * 提取文件名的扩展名。
     *
     * @param filename 文件名
     * @return 方法处理后的结果
     */
    private String extensionOf(String filename) {
        int dot = filename == null ? -1 : filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 尝试删除对象存储文件并忽略删除异常。
     *
     * @param key 对象存储文件键
     */
    private void deleteQuietly(String key) {
        if (key == null) return;
        try {
            storageService.delete(key);
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 权益时长数值
     * @return 操作是否成功
     */
    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 忽略大小写比较两个字符串。
     *
     * @param left  左侧字符串
     * @param right 右侧字符串
     * @return 操作是否成功
     */
    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    /**
     * 截断过长文本以限制保存长度。
     *
     * @param value 权益时长数值
     * @return 方法处理后的结果
     */
    private String truncate(String value) {
        return value.length() <= 512 ? value : value.substring(0, 512);
    }
}
