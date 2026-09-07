package com.app.service.impl;

import com.app.enums.*;
import com.app.exception.BusinessException;
import com.app.mapper.*;
import com.app.pojo.dto.DeviceDto;
import com.app.pojo.entity.*;
import com.app.pojo.vo.DeviceVo;
import com.app.service.DeviceService;
import com.app.utils.BusinessNumberGenerator;
import com.app.utils.UserTimeZoneUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

/**
 * 设备永久绑定服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class DeviceServiceImpl implements DeviceService {
    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile("^CC-[0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$");
    private static final String GIFT_NAME = "设备绑定赠送存储权益";

    private final DeviceMapper deviceMapper;
    private final DeviceBindingMapper bindingMapper;
    private final PlatformConfigMapper configMapper;
    private final StorageEntitlementMapper entitlementMapper;
    private final UserStorageAccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final BusinessNumberGenerator numberGenerator = new BusinessNumberGenerator();

    public DeviceServiceImpl(DeviceMapper deviceMapper, DeviceBindingMapper bindingMapper,
                             PlatformConfigMapper configMapper, StorageEntitlementMapper entitlementMapper,
                             UserStorageAccountMapper accountMapper, PasswordEncoder passwordEncoder) {
        this.deviceMapper = deviceMapper;
        this.bindingMapper = bindingMapper;
        this.configMapper = configMapper;
        this.entitlementMapper = entitlementMapper;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public DeviceVo.MyDevice getMyDevice(Long userId) {
        DeviceBindingEntity binding = bindingMapper.selectByUserId(userId);
        if (binding == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_NOT_FOUND);
        }
        DeviceEntity device = deviceMapper.selectById(binding.getDeviceId());
        if (device == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_NOT_FOUND);
        }
        return toMyDevice(binding, device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceVo.BindResult bindDevice(Long userId, DeviceDto.BindDevice dto) {
        String businessDeviceId = requireDeviceId(dto == null ? null : dto.getDeviceId());
        UserEntity user = bindingMapper.selectUserForUpdate(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_REQUIRED);
        }
        String timeZone = user.getTimeZone();
        UserTimeZoneUtil.requireIanaZone(timeZone);

        DeviceEntity device = deviceMapper.selectByDeviceIdForUpdate(businessDeviceId);
        if (device == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_NOT_FOUND);
        }
        if (device.getStatus() == DeviceStatusEnum.DISABLED) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), device.getInitialPasswordHash())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_CREDENTIAL_INVALID);
        }
        if (bindingMapper.selectByUserId(userId) != null) {
            throw new BusinessException(ErrorCodeEnum.USER_ALREADY_BOUND_DEVICE);
        }
        if (device.getStatus() != DeviceStatusEnum.UNBOUND || bindingMapper.selectByDeviceId(device.getId()) != null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_ALREADY_BOUND);
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        PlatformConfigEntity config = configMapper.selectCurrentActive(now);
        if (config == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_PLATFORM_CONFIG_NOT_FOUND);
        }
        DeviceBindingEntity binding = new DeviceBindingEntity().setUserId(userId).setDeviceId(device.getId())
                .setBindTime(now).setBindUserTimeZone(timeZone).setStatus(DeviceBindingStatusEnum.BOUND);
        try {
            bindingMapper.insert(binding);
            if (deviceMapper.markBoundIfUnbound(device.getId(), DeviceStatusEnum.UNBOUND, DeviceStatusEnum.BOUND) != 1) {
                throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_CONFLICT);
            }
            StorageEntitlementEntity entitlement = createGift(userId, binding.getId(), timeZone, now, config);
            entitlementMapper.insert(entitlement);
            accountMapper.insert(new UserStorageAccountEntity().setUserId(userId).setUsedBytes(0L)
                    .setReservedBytes(0L).setLockVersion(0L));
            DeviceVo.BindResult result = new DeviceVo.BindResult();
            result.setGiftCapacityBytes(entitlement.getCapacityBytes()).setGiftExpireTime(entitlement.getExpireTime())
                    .setBindingId(binding.getId()).setDeviceId(device.getDeviceId()).setModel(device.getModel())
                    .setStatus(DeviceBindingStatusEnum.BOUND.getValue()).setBindTime(now);
            return result;
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_CONFLICT);
        }
    }

    /**
     * 根据平台配置创建设备绑定赠送的存储权益。
     *
     * @param userId    用户ID
     * @param bindingId 设备绑定记录ID
     * @param timeZone  用户时区
     * @param now       权益生效时间
     * @param config    平台存储权益配置
     * @return 设备绑定赠送的存储权益实体
     */
    private StorageEntitlementEntity createGift(Long userId, Long bindingId, String timeZone,
                                                LocalDateTime now, PlatformConfigEntity config) {
        if (config.getDeviceGiftCapacityBytes() == null || config.getDeviceGiftCapacityBytes() <= 0
                || config.getDeviceGiftDurationValue() == null || config.getDeviceGiftDurationValue() <= 0
                || config.getDeviceGiftDurationUnit() == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_PLATFORM_CONFIG_NOT_FOUND);
        }
        Instant effective = now.toInstant(ZoneOffset.UTC);
        Instant expiry = calculateExpiry(effective, timeZone, config.getDeviceGiftDurationValue(),
                config.getDeviceGiftDurationUnit());
        return new StorageEntitlementEntity().setEntitlementNo(numberGenerator.generate("ENT"))
                .setUserId(userId).setSourceType(EntitlementSourceTypeEnum.DEVICE_GIFT)
                .setDeviceBindingId(bindingId).setNameSnapshot(GIFT_NAME)
                .setCapacityBytes(config.getDeviceGiftCapacityBytes())
                .setDurationValue(config.getDeviceGiftDurationValue()).setDurationUnit(config.getDeviceGiftDurationUnit())
                .setUserTimeZoneSnapshot(timeZone).setEffectiveTime(now)
                .setExpireTime(LocalDateTime.ofInstant(expiry, ZoneOffset.UTC)).setStatus(EntitlementStatusEnum.ACTIVE);
    }

    /**
     * 根据权益单位和用户时区计算权益到期时间。
     *
     * @param effective 权益生效时间
     * @param timeZone  用户时区
     * @param value     权益时长数值
     * @param unit      权益时长单位
     * @return 按指定单位计算出的权益到期时间
     */
    private Instant calculateExpiry(Instant effective, String timeZone, int value, DurationUnitEnum unit) {
        return switch (unit) {
            case DAY -> effective.atZone(UserTimeZoneUtil.requireIanaZone(timeZone)).plusDays(value).toInstant();
            case MONTH -> UserTimeZoneUtil.calculateNaturalMonthExpiry(effective, timeZone, value);
            case YEAR -> UserTimeZoneUtil.calculateNaturalYearExpiry(effective, timeZone, value);
        };
    }

    /**
     * 将设备实体和绑定实体转换为设备视图对象。
     *
     * @param binding 设备绑定记录
     * @param device  设备记录
     * @return 前端设备视图对象
     */
    private DeviceVo.MyDevice toMyDevice(DeviceBindingEntity binding, DeviceEntity device) {
        return new DeviceVo.MyDevice().setBindingId(binding.getId()).setDeviceId(device.getDeviceId())
                .setModel(device.getModel()).setStatus(binding.getStatus().getValue()).setBindTime(binding.getBindTime());
    }

    /**
     * 校验并规范化业务设备编号。
     *
     * @param deviceId 设备ID
     * @return 方法处理后的结果
     */
    private String requireDeviceId(String deviceId) {
        String normalized = deviceId == null ? "" : deviceId.trim();
        if (!DEVICE_ID_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_ID_INVALID);
        }
        return normalized;
    }
}
