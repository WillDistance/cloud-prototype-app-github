package com.app.service.impl;

import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.mapper.*;
import com.app.pojo.dto.DeviceUploadDto;
import com.app.pojo.entity.*;
import com.app.pojo.vo.DeviceUploadVo;
import com.app.service.DeviceUploadService;
import com.app.support.objectstorage.UploadUrlSigner;
import com.app.utils.BusinessNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 绑定设备上传会话服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class DeviceUploadServiceImpl implements DeviceUploadService {
    private static final int DEFAULT_URL_TTL_SECONDS = 600;
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private final DeviceMapper deviceMapper;
    private final DeviceBindingMapper bindingMapper;
    private final PlatformConfigMapper configMapper;
    private final UserStorageAccountMapper accountMapper;
    private final StorageEntitlementMapper entitlementMapper;
    private final UploadSessionMapper sessionMapper;
    private final PasswordEncoder passwordEncoder;
    private final UploadUrlSigner uploadUrlSigner;
    private final Supplier<LocalDateTime> nowSupplier;
    private final int urlTtlSeconds;
    private final BusinessNumberGenerator numberGenerator = new BusinessNumberGenerator();
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public DeviceUploadServiceImpl(DeviceMapper deviceMapper, DeviceBindingMapper bindingMapper,
                                   PlatformConfigMapper configMapper, UserStorageAccountMapper accountMapper,
                                   StorageEntitlementMapper entitlementMapper, UploadSessionMapper sessionMapper,
                                   PasswordEncoder passwordEncoder, UploadUrlSigner uploadUrlSigner) {
        this(deviceMapper, bindingMapper, configMapper, accountMapper, entitlementMapper, sessionMapper,
                passwordEncoder, uploadUrlSigner, () -> LocalDateTime.now(ZoneOffset.UTC), DEFAULT_URL_TTL_SECONDS);
    }

    public DeviceUploadServiceImpl(DeviceMapper deviceMapper, DeviceBindingMapper bindingMapper,
                                   PlatformConfigMapper configMapper, UserStorageAccountMapper accountMapper,
                                   StorageEntitlementMapper entitlementMapper, UploadSessionMapper sessionMapper,
                                   PasswordEncoder passwordEncoder, UploadUrlSigner uploadUrlSigner,
                                   Supplier<LocalDateTime> nowSupplier, int urlTtlSeconds) {
        this.deviceMapper = deviceMapper;
        this.bindingMapper = bindingMapper;
        this.configMapper = configMapper;
        this.accountMapper = accountMapper;
        this.entitlementMapper = entitlementMapper;
        this.sessionMapper = sessionMapper;
        this.passwordEncoder = passwordEncoder;
        this.uploadUrlSigner = uploadUrlSigner;
        this.nowSupplier = nowSupplier;
        this.urlTtlSeconds = urlTtlSeconds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceUploadVo.CreateSession createUploadSession(DeviceUploadDto.CreateSession dto) {
        LocalDateTime now = nowSupplier.get();
        AuthenticatedDevice authenticated = authenticate(dto == null ? null : dto.getDeviceId(), dto == null ? null : dto.getPassword());
        validateFile(dto, configMapper.selectCurrentActive(now));
        UserStorageAccountEntity account = accountMapper.selectByUserIdForUpdate(authenticated.binding().getUserId());
        if (account == null) throw new BusinessException(ErrorCodeEnum.STORAGE_ACCOUNT_NOT_FOUND);
        Long capacity = entitlementMapper.sumActiveCapacity(authenticated.binding().getUserId(), now);
        long declared = dto.getFileSizeBytes();
        if (capacity == null || account.getUsedBytes() + account.getReservedBytes() + declared > capacity) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_CAPACITY_INSUFFICIENT);
        }
        LocalDateTime expiry = now.plusSeconds(urlTtlSeconds);
        String extension = extensionOf(dto.getOriginalFileName());
        UploadSessionEntity session = new UploadSessionEntity()
                .setUploadNo(numberGenerator.generate("UPL"))
                .setRequestSource(UploadRequestSourceEnum.BOUND_DEVICE).setDeviceAuthType(DeviceAuthTypeEnum.DEVICE_PASSWORD)
                .setUserId(authenticated.binding().getUserId()).setDeviceId(authenticated.device().getId())
                .setDeviceBindingId(authenticated.binding().getId()).setDeviceAuthenticatedTime(now)
                .setUserTimeZoneSnapshot(authenticated.binding().getBindUserTimeZone()).setOriginalFileName(dto.getOriginalFileName())
                .setDeclaredContentType(dto.getContentType()).setDeclaredFileSizeBytes(declared).setReservedBytes(declared)
                .setOriginalObjectKey(objectKey(authenticated.binding().getUserId(), authenticated.device().getId(), now, extension))
                .setUploadUrlExpireTime(expiry).setStatus(UploadSessionStatusEnum.URL_ISSUED);
        accountMapper.increaseReservedBytes(authenticated.binding().getUserId(), declared);
        sessionMapper.insert(session);
        String url = uploadUrlSigner.createPutUrl(session.getOriginalObjectKey(), dto.getContentType(), expiry,
                session.getUploadNo());
        return new DeviceUploadVo.CreateSession().setUploadNo(session.getUploadNo()).setUploadUrl(url).setMethod("PUT")
                .setRequiredHeaders(Map.of("Content-Type", dto.getContentType())).setExpireTime(expiry);
    }

    @Override
    public DeviceUploadVo.SessionStatus getUploadSessionStatus(DeviceUploadDto.SessionStatus dto) {
        AuthenticatedDevice authenticated = authenticate(dto == null ? null : dto.getDeviceId(), dto == null ? null : dto.getPassword());
        UploadSessionEntity session = sessionMapper.selectByUploadNoAndDeviceId(dto.getUploadNo(), authenticated.device().getId());
        if (session == null || !Objects.equals(session.getDeviceBindingId(), authenticated.binding().getId())) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_UPLOAD_SESSION_INVALID);
        }
        return new DeviceUploadVo.SessionStatus().setUploadNo(session.getUploadNo()).setStatus(session.getStatus().getValue())
                .setOriginalFileName(session.getOriginalFileName()).setDeclaredFileSizeBytes(session.getDeclaredFileSizeBytes())
                .setExpireTime(session.getUploadUrlExpireTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseExpiredReservations(LocalDateTime now, int batchSize) {
        if (batchSize <= 0) return 0;
        int released = 0;
        for (UploadSessionEntity session : sessionMapper.selectExpiredForUpdate(now, batchSize)) {
            if (sessionMapper.markExpiredIfUrlIssued(session.getId()) == 1) {
                UserStorageAccountEntity account = accountMapper.selectByUserIdForUpdate(session.getUserId());
                if (account == null || accountMapper.decreaseReservedBytes(session.getUserId(), session.getReservedBytes()) != 1) {
                    throw new BusinessException(ErrorCodeEnum.SYSTEM_OPERATION_CONFLICT);
                }
                released++;
            }
        }
        return released;
    }

    /**
     * 校验设备编号和设备密码，并返回认证后的设备绑定信息。
     *
     * @param deviceId 设备ID
     * @param password 设备密码
     * @return 方法处理后的结果
     */
    private AuthenticatedDevice authenticate(String deviceId, String password) {
        DeviceEntity device = deviceMapper.selectByBusinessDeviceId(deviceId == null ? "" : deviceId.trim());
        if (device == null) throw new BusinessException(ErrorCodeEnum.DEVICE_NOT_FOUND);
        if (device.getStatus() == DeviceStatusEnum.DISABLED) throw new BusinessException(ErrorCodeEnum.DEVICE_DISABLED);
        if (device.getStatus() != DeviceStatusEnum.BOUND)
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_NOT_FOUND);
        if (password == null || !passwordEncoder.matches(password, device.getInitialPasswordHash())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_CREDENTIAL_INVALID);
        }
        DeviceBindingEntity binding = bindingMapper.selectByDeviceId(device.getId());
        if (binding == null || binding.getStatus() != DeviceBindingStatusEnum.BOUND)
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_NOT_FOUND);
        return new AuthenticatedDevice(device, binding);
    }

    /**
     * 校验上传文件的名称、类型和大小。
     *
     * @param dto    请求参数
     * @param config 平台存储权益配置
     */
    private void validateFile(DeviceUploadDto.CreateSession dto, PlatformConfigEntity config) {
        if (dto == null || config == null || dto.getFileSizeBytes() == null || dto.getFileSizeBytes() <= 0
                || dto.getOriginalFileName() == null || dto.getContentType() == null || config.getMaxFileSizeBytes() == null
                || dto.getFileSizeBytes() > config.getMaxFileSizeBytes() || !containsIgnoreCase(config.getAllowedExtensions(), extensionOf(dto.getOriginalFileName()))
                || !containsIgnoreCase(config.getAllowedMimeTypes(), dto.getContentType())) {
            throw new BusinessException(ErrorCodeEnum.PHOTO_FILE_INVALID);
        }
    }

    /**
     * 忽略大小写判断集合中是否包含指定值。
     *
     * @param values 待匹配的字符串集合
     * @param target 待匹配的目标值
     * @return 操作是否成功
     */
    private boolean containsIgnoreCase(List<String> values, String target) {
        return values != null && values.stream().anyMatch(value -> value.equalsIgnoreCase(target));
    }

    /**
     * 提取文件名的扩展名。
     *
     * @param filename 文件名
     * @return 方法处理后的结果
     */
    private String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot < 1 || dot == filename.length() - 1 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 生成设备上传文件在对象存储中的键。
     *
     * @param userId    用户ID
     * @param deviceId  设备ID
     * @param now       当前UTC时间
     * @param extension 文件扩展名
     * @return 方法处理后的结果
     */
    private String objectKey(Long userId, Long deviceId, LocalDateTime now, String extension) {
        byte[] random = new byte[16];
        secureRandom.nextBytes(random);
        StringBuilder value = new StringBuilder(32);
        for (byte b : random) {
            value.append(HEX[(b >>> 4) & 15]).append(HEX[b & 15]);
        }
        return "users/" + userId + "/devices/" + deviceId + "/original/" + now.getYear() + "/" +
                String.format(Locale.ROOT, "%02d", now.getMonthValue()) + "/" + value + "." + extension;
    }

    /**
     * 封装已通过设备认证的设备和绑定关系。
     *
     * @param device  设备记录
     * @param binding 设备绑定记录
     * @return 方法处理后的结果
     */
    private record AuthenticatedDevice(DeviceEntity device, DeviceBindingEntity binding) {
    }
}
