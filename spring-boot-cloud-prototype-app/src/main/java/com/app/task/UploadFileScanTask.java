package com.app.task;

import com.app.constants.RedisKeyConstants;
import com.app.enums.DeleteReasonEnum;
import com.app.enums.ErrorCodeEnum;
import com.app.enums.PhotoFileStatusEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.exception.BusinessException;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.support.oss.FileMetadata;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import com.app.utils.RedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 上传文件扫描定时任务
 * 扫描待上传原图、生成缩略/预览图、清理超时上传、异步清理删除文件
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Slf4j
@Component
public class UploadFileScanTask {
    /**
     * 上传超时阈值：30分钟
     */
    private static final Duration UPLOAD_EXPIRE_DURATION = Duration.ofMinutes(30);
    /**
     * 缩略图长边最大像素
     */
    @Value("${thumbnailMaxEdge:480}")
    private Integer thumbnailMaxEdge;
    /**
     * 预览图长边最大像素
     */
    @Value("${previewMaxEdge:1920}")
    private Integer previewMaxEdge;

    @Autowired
    private PhotoFileMapper photoFileMapper;

    @Autowired
    private UserStorageAccountMapper storageAccountMapper;

    @Autowired
    private ObsFileService obsFileService;

    @Autowired
    private ObsProperties obsProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Lazy
    @Autowired
    private UploadFileScanTask self;

    /**
     * 定时扫描处理待上传原图记录
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void scanPendingUploadPhotoTask() {
        // 分布式锁，多实例部署避免重复执行任务
        String redisBusinessKey = "scanPendingUploadPhotoTask";
        String lockVal = redisLockUtil.tryGetLock(redisBusinessKey, 1, 300, TimeUnit.SECONDS);
        if (Objects.isNull(lockVal)) {
            log.info("文件上传扫描任务正在执行，跳过本次调度");
            return;
        }
        try {
            LocalDateTime nowUtc = LocalDateTime.now();
            log.debug("开始执行文件上传扫描任务，UTC当前时间:{}", nowUtc);

            scanPendingUploadPhoto();

            log.debug("文件上传扫描任务执行完成");
        } catch (Exception e) {
            log.error("上传文件扫描任务全局异常", e);
        } finally {
            // 释放分布式锁
            redisLockUtil.releaseLock(redisBusinessKey, lockVal);
        }
    }

    /**
     * 定时扫描处理待删除/删除失败文件
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void scanPendingDeletePhotoTask() {
        // 分布式锁，多实例部署避免重复执行任务
        String redisBusinessKey = "scanPendingDeletePhotoTask";
        String lockVal = redisLockUtil.tryGetLock(redisBusinessKey, 1, 300, TimeUnit.SECONDS);
        if (Objects.isNull(lockVal)) {
            log.error("文件上传扫描任务正在执行，跳过本次调度");
            return;
        }
        try {
            log.info("开始执行文件上传扫描任务，UTC当前时间:{}，存储桶:{}", LocalDateTime.now());

            scanPendingDeletePhoto();

            log.info("文件上传扫描任务执行完成");
        } catch (Exception e) {
            log.error("上传文件扫描任务全局异常", e);
        } finally {
            // 释放分布式锁
            redisLockUtil.releaseLock(redisBusinessKey, lockVal);
        }
    }


    /**
     * 分页扫描待上传原图记录
     */
    private void scanPendingUploadPhoto() {
        // 建议实现分页查询 selectPendingUploadsPage，防止表数据量大全量加载OOM
        List<PhotoFileEntity> pendingPhotoList = photoFileMapper.selectPendingUploads(PhotoFileStatusEnum.URL_ISSUED.getValue());
        for (PhotoFileEntity photo : pendingPhotoList) {
            try {
                handleSinglePendingPhoto(photo);
            } catch (Exception e) {
                log.error("校验待上传文件发生存储异常，等待下一轮重试，userId:{}, objectKey:{}", photo.getUserId(), photo.getObjectKey(), e);
            }
        }
    }

    /**
     * 单条原图上传状态校验与处理
     */
    public void handleSinglePendingPhoto(PhotoFileEntity photo) {
        String objectKey = photo.getObjectKey();

        // 校验对象存储文件是否存在
        boolean objectExists = obsFileService.existObject(obsProperties.getBucketName(), objectKey);
        if (!objectExists) {
            // 判断上传链接是否超时
            LocalDateTime nowUtc = LocalDateTime.now();
            LocalDateTime expireThreshold = nowUtc.minus(UPLOAD_EXPIRE_DURATION);
            if (photo.getUploadUrlExpireTime().isBefore(expireThreshold)) {
                log.warn("文件上传超时，执行清理，userId:{}, objectKey:{}", photo.getUserId(), objectKey);
                self.deleteUploadPhoto(photo);
            }
            return;
        }

        // 获取文件元信息校验大小、文件类型
        FileMetadata metadata = obsFileService.statObject(obsProperties.getBucketName(), objectKey);
        if (metadata == null) {
            log.warn("文件元数据查询为空，等待下一轮扫描，objectKey:{}", objectKey);
            return;
        }
        // 文件大小/类型不匹配，判定上传异常，清理记录
        if (metadata.getSize() != photo.getSizeBytes() || !photo.getMimeType().equalsIgnoreCase(metadata.getContentType())) {
            log.warn("上传文件校验不匹配，清理记录，userId:{}, objectKey:{}", photo.getUserId(), objectKey);
            self.deleteUploadPhoto(photo);
            return;
        }

        // 文件校验通过，生成缩略图并发布可用状态
        self.publishPhotoToAvailable(photo);

    }

    /**
     * 原图校验通过，生成缩略图、预览图并更新数据库状态
     */
    @Transactional(rollbackFor = Exception.class)
    protected void publishPhotoToAvailable(PhotoFileEntity original) {
        String objectKey = original.getObjectKey();
        try (InputStream inputStream = obsFileService.getObject(obsProperties.getBucketName(), objectKey)) {
            BufferedImage sourceImage = ImageIO.read(inputStream);
            if (sourceImage == null) {
                log.error("原图读取失败，图片损坏，清理记录 userId:{}, key:{}", original.getUserId(), objectKey);
                deleteUploadPhoto(original);
                return;
            }

            // 构造缩略图、预览图存储路径
            String baseDir = objectKey.substring(0, objectKey.lastIndexOf('/') + 1);
            String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
            String thumbnailKey = baseDir + "thumbnail/" + fileName;
            String previewKey = baseDir + "preview/" + fileName;

            // 生成并上传派生图，派生图不落库，访问路径由原图路径推导
            saveDerivedImageFile(sourceImage, thumbnailKey, thumbnailMaxEdge);
            saveDerivedImageFile(sourceImage, previewKey, previewMaxEdge);

            // 更新原图状态为可用
            photoFileMapper.updateStatusById(original.getId(), PhotoFileStatusEnum.AVAILABLE.getValue(), PhotoFileStatusEnum.URL_ISSUED.getValue());
            // 容量账目变更：释放预留容量，占用实际存储容量
            storageAccountMapper.decreaseReservedBytes(original.getUserId(), original.getSizeBytes());
            storageAccountMapper.increaseUsedBytes(original.getUserId(), original.getSizeBytes());

            log.info("原图上传完成，生成缩略/预览图成功，userId:{}, objectKey:{}", original.getUserId(), objectKey);
        } catch (IOException e) {
            log.error("读取原图IO异常，清理上传记录 userId:{}, key:{}", original.getUserId(), objectKey, e);
            deleteUploadPhoto(original);
        } catch (Exception e) {
            log.error("发布原图流程异常，清理上传记录 userId:{}, key:{}", original.getUserId(), objectKey, e);
            deleteUploadPhoto(original);
        }
    }

    /**
     * 生成缩放图并上传MinIO，不写数据库记录
     */
    private void saveDerivedImageFile(BufferedImage sourceImg, String targetKey, int maxEdge) throws Exception {
        // 图片等比例缩放
        BufferedImage scaledImg = scaleImage(sourceImg, maxEdge);
        // 图片写入字节数组
        byte[] imageBytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            String suffix = targetKey.substring(targetKey.lastIndexOf('/') + 1);
            suffix = suffix.substring(suffix.lastIndexOf('.') + 1);
            boolean writeSuccess = ImageIO.write(scaledImg, suffix, bos);
            if (!writeSuccess) {
                log.error("图片写入字节流失败，格式:" + suffix);
                throw new BusinessException(ErrorCodeEnum.E00004);
            }
            imageBytes = bos.toByteArray();
        }

        // 上传对象存储
        obsFileService.putObjectByte(obsProperties.getBucketName(), targetKey, imageBytes);
    }

    /**
     * 图片等比例缩放工具方法，自动释放绘图资源
     */
    private BufferedImage scaleImage(BufferedImage source, int maxEdge) {
        double ratio = Math.min(1D, (double) maxEdge / Math.max(source.getWidth(), source.getHeight()));
        int targetW = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int targetH = Math.max(1, (int) Math.round(source.getHeight() * ratio));

        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = null;
        try {
            g2d = result.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(source, 0, 0, targetW, targetH, null);
        } finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }
        return result;
    }

    /**
     * 清理超时未完成上传的原图记录，删除存储文件、释放预留容量
     */
    @Transactional
    protected void deleteUploadPhoto(PhotoFileEntity photo) {
        String objectKey = photo.getObjectKey();
        try {
            // 存在文件则删除
            if (obsFileService.existObject(obsProperties.getBucketName(), objectKey)) {
                obsFileService.removeObject(obsProperties.getBucketName(), objectKey);
                log.info("清理超时上传原图存储文件成功，key:{}", objectKey);
            }
        } catch (Exception e) {
            log.error("清理超时文件存储删除失败，保留记录等待下次重试，key:{}", objectKey, e);
            return;
        }
        // 删除数据库记录、清理下载链接缓存、释放预留存储空间
        photoFileMapper.deleteById(photo.getId());
        redisTemplate.delete(RedisKeyConstants.OBJECT_DOWNLOAD_URL_PREFIX + objectKey);
        storageAccountMapper.decreaseReservedBytes(photo.getUserId(), photo.getSizeBytes());
        log.info("超时上传记录清理完成 userId:{}, objectKey:{}", photo.getUserId(), objectKey);
    }

    /**
     * 扫描待删除、删除失败原图分组批量删除存储文件
     */
    private void scanPendingDeletePhoto() {
        List<String> deleteStatusList = List.of(PhotoFileStatusEnum.DELETE_PENDING.getValue(), PhotoFileStatusEnum.DELETE_FAILED.getValue());
        List<PhotoFileEntity> allDeleteList = new ArrayList<>();
        for (String status : deleteStatusList) {
            allDeleteList.addAll(photoFileMapper.selectByStatus(status));
        }
        // 只处理原图类型，缩略图/预览图跟随原图批量删除
        for (PhotoFileEntity photo : allDeleteList) {
            if (PhotoFileTypeEnum.ORIGINAL.getValue().equals(photo.getFileType())) {
                self.batchDeletePhotoGroup(photo);
            }
        }
    }

    /**
     * 删除原图对应整组文件（原图+缩略+预览）
     */
    @Transactional(rollbackFor = Exception.class)
    protected void batchDeletePhotoGroup(PhotoFileEntity original) {
        Long userId = original.getUserId();
        String groupKey = original.getObjectKey();
        boolean deleteAllSuccess = true;

        // 删除原图以及由原图路径推导出的缩略图和预览图
        String fileName = groupKey.substring(groupKey.lastIndexOf('/') + 1);
        String baseDir = groupKey.substring(0, groupKey.lastIndexOf('/') + 1);
        List<String> objectKeys = List.of(groupKey, baseDir + "thumbnail/" + fileName, baseDir + "preview/" + fileName);
        for (String key : objectKeys) {
            try {
                if (obsFileService.existObject(obsProperties.getBucketName(), key)) {
                    obsFileService.removeObject(obsProperties.getBucketName(), key);
                }
            } catch (Exception e) {
                deleteAllSuccess = false;
                log.error("删除文件存储失败，标记删除失败 userId:{}, key:{}", userId, key, e);
            }
        }

        // 存在删除失败，原图标记失败，等待下次扫描重试
        if (!deleteAllSuccess) {
            String failReason = original.getDeleteReason() == null ? DeleteReasonEnum.USER_MANUAL.getValue() : original.getDeleteReason();
            photoFileMapper.updateDeleteStatus(original.getId(), PhotoFileStatusEnum.DELETE_FAILED.getValue(), failReason);
            return;
        }

        // 全部删除成功：删除原图数据库记录、清理缓存、扣减容量
        photoFileMapper.deleteById(original.getId());
        for (String key : objectKeys) {
            redisTemplate.delete(RedisKeyConstants.OBJECT_DOWNLOAD_URL_PREFIX + key);
        }
        storageAccountMapper.decreaseUsedBytes(userId, original.getSizeBytes());
        log.info("图片分组全部删除完成 userId:{}, groupKey:{}", userId, groupKey);
    }
}
