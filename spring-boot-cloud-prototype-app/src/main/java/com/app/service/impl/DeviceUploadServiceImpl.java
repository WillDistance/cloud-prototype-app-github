package com.app.service.impl;

import com.app.enums.DeviceStatusEnum;
import com.app.enums.ErrorCodeEnum;
import com.app.enums.PhotoFileStatusEnum;
import com.app.enums.PhotoFileTypeEnum;
import com.app.exception.BusinessException;
import com.app.mapper.DeviceBindingMapper;
import com.app.mapper.DeviceMapper;
import com.app.mapper.PhotoFileMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.dto.DeviceUploadRequest;
import com.app.pojo.entity.DeviceEntity;
import com.app.pojo.entity.DeviceBindingEntity;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.DeviceUploadVo;
import com.app.service.DeviceUploadService;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备上传业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class DeviceUploadServiceImpl extends ServiceImpl<PhotoFileMapper, PhotoFileEntity> implements DeviceUploadService {
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private DeviceBindingMapper bindingMapper;
    @Autowired
    private UserStorageAccountMapper storageAccountMapper;
    @Autowired
    private PhotoFileMapper photoFileMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObsFileService obsFileService;
    @Autowired
    private ObsProperties obsProperties;

    @Override
    @Transactional
    public DeviceUploadVo createUploadSession(DeviceUploadRequest request) {
        DeviceEntity device = deviceMapper.selectByDeviceId(request.getDeviceId());
        if (device == null || !DeviceStatusEnum.BOUND.getValue().equals(device.getStatus())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), device.getInitialPasswordHash())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_CREDENTIAL_INVALID);
        }
        DeviceBindingEntity binding = bindingMapper.selectByDeviceId(device.getId());
        if (binding == null || !binding.getDeviceId().equals(device.getId())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_NOT_FOUND);
        }
        UserStorageAccountEntity account = storageAccountMapper.selectByUserIdForUpdate(binding.getUserId());
        if (account == null || account.getUsedBytes() + account.getReservedBytes() + request.getSizeBytes() < account.getUsedBytes()) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_CAPACITY_INSUFFICIENT);
        }
        if (storageAccountMapper.increaseReservedBytes(binding.getUserId(), request.getSizeBytes()) != 1) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_CAPACITY_INSUFFICIENT);
        }
        LocalDateTime expireTime = LocalDateTime.now().plus(UPLOAD_URL_TTL);
        String objectKey = "original/" + binding.getUserId() + "/" + IdWorker.getId() + "." + request.getExtension().toLowerCase();
        PhotoFileEntity photo = new PhotoFileEntity().setId(IdWorker.getId()).setUserId(binding.getUserId()).setDeviceId(device.getId())
                .setFileName(request.getFileName()).setExtension(request.getExtension().toLowerCase()).setMimeType(request.getMimeType())
                .setSizeBytes(request.getSizeBytes()).setUserTimeZoneSnapshot(binding.getBindUserTimeZone())
                .setUploadUrlExpireTime(expireTime).setFileType(PhotoFileTypeEnum.ORIGINAL.getValue()).setObjectKey(objectKey)
                .setStatus(PhotoFileStatusEnum.URL_ISSUED.getValue());
        photoFileMapper.insert(photo);

        String uploadUrl = obsFileService.generateUploadPresignedUrl(obsProperties.getBucketName(), objectKey, UPLOAD_URL_TTL,
                Map.of(), Map.of("Content-Type", request.getMimeType()));
        return new DeviceUploadVo(photo.getId(), objectKey, uploadUrl, "PUT", Map.of("Content-Type", request.getMimeType()), expireTime);
    }
}
