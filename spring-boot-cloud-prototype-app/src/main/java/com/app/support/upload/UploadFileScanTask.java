package com.app.support.upload;

import com.app.enums.PhotoFileStatusEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.support.oss.FileMetadata;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MinIO上传文件扫描任务。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Component
public class UploadFileScanTask {
    private static final LocalDateTime MIN_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    @Autowired
    private PhotoFileMapper photoFileMapper;
    @Autowired
    private UserStorageAccountMapper storageAccountMapper;
    @Autowired
    private ObsFileService obsFileService;
    @Autowired
    private ObsProperties obsProperties;

    /**
     * 定时扫描待上传原图，处理已上传对象并清理长时间未完成的记录。
     */
    @Scheduled(fixedDelayString = "${upload.scan.fixed-delay:60000}")
    public void scanUploads() {
        List<PhotoFileEntity> pending = photoFileMapper.selectPendingUploads(PhotoFileStatusEnum.URL_ISSUED.getValue());
        for (PhotoFileEntity photo : pending) {
            processPendingPhoto(photo);
        }
    }

    /**
     * 检查单条原图记录对应的MinIO对象并决定处理或清理。
     *
     * @param photo 待检查的原图记录
     */
    protected void processPendingPhoto(PhotoFileEntity photo) {
        FileMetadata metadata = obsFileService.statObject(obsProperties.getBucketName(), photo.getObjectKey());
        if (metadata == null) {
            if (photo.getUploadUrlExpireTime().isBefore(LocalDateTime.now().minusMinutes(30))) {
                cleanupExpiredPhoto(photo);
            }
            return;
        }
        if (metadata.getSize() != photo.getSizeBytes() || !photo.getMimeType().equalsIgnoreCase(metadata.getContentType())) {
            cleanupExpiredPhoto(photo);
            return;
        }
        publishPhoto(photo);
    }

    /**
     * 读取原图、生成缩略图和预览图，并将完整文件组发布为可访问状态。
     *
     * @param original 原图记录
     */
    @Transactional
    protected void publishPhoto(PhotoFileEntity original) {
        try (InputStream input = obsFileService.getObject(obsProperties.getBucketName(), original.getObjectKey())) {
            BufferedImage source = ImageIO.read(input);
            if (source == null) {
                cleanupExpiredPhoto(original);
                return;
            }
            String baseKey = original.getObjectKey().substring(0, original.getObjectKey().lastIndexOf('/') + 1);
            String fileName = original.getObjectKey().substring(original.getObjectKey().lastIndexOf('/') + 1);
            String thumbnailKey = baseKey + "thumbnail/" + fileName;
            String previewKey = baseKey + "preview/" + fileName;
            saveDerived(original, source, thumbnailKey, PhotoFileTypeEnum.THUMBNAIL, 480);
            saveDerived(original, source, previewKey, PhotoFileTypeEnum.PREVIEW, 1920);
            photoFileMapper.updateStatusById(original.getId(), PhotoFileStatusEnum.AVAILABLE.getValue(), PhotoFileStatusEnum.URL_ISSUED.getValue());
            storageAccountMapper.decreaseReservedBytes(original.getUserId(), original.getSizeBytes());
            storageAccountMapper.increaseUsedBytes(original.getUserId(), original.getSizeBytes());
        } catch (Exception exception) {
            cleanupExpiredPhoto(original);
        }
    }

    /**
     * 缩放并上传一个派生图文件，同时保存其文件记录。
     *
     * @param original 原图记录
     * @param source 原图像素数据
     * @param objectKey 派生图对象路径
     * @param fileType 派生图类型
     * @param maxEdge 长边最大像素数
     */
    private void saveDerived(PhotoFileEntity original, BufferedImage source, String objectKey, PhotoFileTypeEnum fileType, int maxEdge) throws Exception {
        BufferedImage derived = resize(source, maxEdge);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(derived, "jpg", output);
        byte[] content = output.toByteArray();
        obsFileService.putObjectByte(obsProperties.getBucketName(), objectKey, content);
        PhotoFileEntity file = new PhotoFileEntity().setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId())
                .setUserId(original.getUserId()).setDeviceId(original.getDeviceId()).setFileName(original.getFileName())
                .setExtension("jpg").setMimeType("image/jpeg").setSizeBytes((long) content.length)
                .setUserTimeZoneSnapshot(original.getUserTimeZoneSnapshot()).setUploadUrlExpireTime(original.getUploadUrlExpireTime())
                .setFileType(fileType.getValue()).setObjectKey(objectKey).setStatus(PhotoFileStatusEnum.AVAILABLE.getValue())
                .setUploadedTime(LocalDateTime.now());
        photoFileMapper.insert(file);
    }

    /**
     * 按长边限制等比例缩放图片。
     *
     * @param source 原始图片
     * @param maxEdge 长边最大像素数
     * @return 缩放后的图片
     */
    private BufferedImage resize(BufferedImage source, int maxEdge) {
        double ratio = Math.min(1D, (double) maxEdge / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return result;
    }

    /**
     * 删除超时上传记录和可能存在的原图对象，并释放预留容量。
     *
     * @param photo 超时的原图记录
     */
    @Transactional
    protected void cleanupExpiredPhoto(PhotoFileEntity photo) {
        try {
            obsFileService.removeObject(obsProperties.getBucketName(), photo.getObjectKey());
        } catch (Exception ignored) {
            return;
        }
        photoFileMapper.deleteById(photo.getId());
        storageAccountMapper.decreaseReservedBytes(photo.getUserId(), photo.getSizeBytes());
    }
}
