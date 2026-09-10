package com.app.service.impl;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.PhotoFileStatusEnum;
import com.app.exception.OssCallbackRejectedException;
import com.app.mapper.PhotoFileMapper;
import com.app.pojo.dto.OssUploadCompletedRequest;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.service.OssCallbackService;
import com.app.support.oss.FileMetadata;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import com.app.task.UploadFileScanTask;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对象存储上传回调业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class OssCallbackServiceImpl extends ServiceImpl<PhotoFileMapper, PhotoFileEntity> implements OssCallbackService {
    @Autowired
    private PhotoFileMapper photoFileMapper;
    @Autowired
    private ObsFileService obsFileService;
    @Autowired
    private ObsProperties obsProperties;
    @Autowired
    private UploadFileScanTask uploadFileScanTask;

    @Override
    @Transactional
    public void uploadCompleted(OssUploadCompletedRequest request) {
        PhotoFileEntity photo = photoFileMapper.selectById(request.getPhotoFileId());
        if (photo == null || !PhotoFileStatusEnum.URL_ISSUED.getValue().equals(photo.getStatus())) {
            throw new OssCallbackRejectedException(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID);
        }
        if (!photo.getObjectKey().equals(request.getObjectKey())) {
            throw new OssCallbackRejectedException(ErrorCodeEnum.PHOTO_FILE_INVALID);
        }
        FileMetadata metadata = obsFileService.statObject(obsProperties.getBucketName(), photo.getObjectKey());
        if (metadata == null || metadata.getSize() != photo.getSizeBytes() || !photo.getMimeType().equalsIgnoreCase(metadata.getContentType())) {
            throw new OssCallbackRejectedException(ErrorCodeEnum.PHOTO_FILE_INVALID);
        }
        uploadFileScanTask.handleSinglePendingPhoto(photo);
    }
}
