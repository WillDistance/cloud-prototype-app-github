package com.app.service.impl;

import com.app.enums.BindingStatusEnum;
import com.app.enums.DeviceStatusEnum;
import com.app.enums.ErrorCodeEnum;
import com.app.exception.BusinessException;
import com.app.mapper.DeviceBindingMapper;
import com.app.mapper.DeviceMapper;
import com.app.mapper.PlatformConfigMapper;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.dto.DeviceBindRequest;
import com.app.pojo.entity.DeviceBindingEntity;
import com.app.pojo.entity.DeviceEntity;
import com.app.pojo.entity.PlatformConfigEntity;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.DeviceVo;
import com.app.service.DeviceService;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 设备绑定业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DeviceEntity> implements DeviceService {
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private DeviceBindingMapper bindingMapper;
    @Autowired
    private PlatformConfigMapper platformConfigMapper;
    @Autowired
    private StorageEntitlementMapper entitlementMapper;
    @Autowired
    private UserStorageAccountMapper storageAccountMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public DeviceVo getMyDevice() {
        DeviceBindingEntity binding = bindingMapper.selectByUserId(UserContextHolderUtil.getUserId());
        if (binding == null) {
            return null;
        }
        DeviceEntity device = deviceMapper.selectById(binding.getDeviceId());
        if (device == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_NOT_FOUND);
        }
        return toVo(device, binding, null);
    }

    @Override
    @Transactional
    public DeviceVo bindDevice(DeviceBindRequest request) {
        Long userId = UserContextHolderUtil.getUserId();
        DeviceEntity device = deviceMapper.selectByDeviceId(request.getDeviceId());
        if (device == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_NOT_FOUND);
        }
        if (device.getInitialPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), device.getInitialPasswordHash())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_CREDENTIAL_INVALID);
        }
        if (bindingMapper.selectByUserId(userId) != null) {
            throw new BusinessException(ErrorCodeEnum.USER_ALREADY_BOUND_DEVICE);
        }
        device = deviceMapper.selectByIdForUpdate(device.getId());
        if (device == null || !DeviceStatusEnum.UNBOUND.getValue().equals(device.getStatus())) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_CONFLICT);
        }
        PlatformConfigEntity config = platformConfigMapper.selectActive();
        if (config == null) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_PLATFORM_CONFIG_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = deviceMapper.updateStatusById(device.getId(), DeviceStatusEnum.BOUND.getValue(), DeviceStatusEnum.UNBOUND.getValue());
        if (updated != 1) {
            throw new BusinessException(ErrorCodeEnum.DEVICE_BINDING_CONFLICT);
        }
        DeviceBindingEntity binding = new DeviceBindingEntity().setId(IdWorker.getId()).setUserId(userId)
                .setDeviceId(device.getId()).setBindTime(now).setBindUserTimeZone(UserContextHolderUtil.get().timeZone())
                .setStatus(BindingStatusEnum.BOUND.getValue());
        bindingMapper.insert(binding);
        UserStorageAccountEntity account = storageAccountMapper.selectByUserIdForUpdate(userId);
        if (account == null) {
            account = new UserStorageAccountEntity().setId(IdWorker.getId()).setUserId(userId).setUsedBytes(0L)
                    .setReservedBytes(0L).setLockVersion(0L);
            storageAccountMapper.insert(account);
        }
        StorageEntitlementEntity entitlement = createGiftEntitlement(userId, config, now);
        entitlementMapper.insert(entitlement);
        device.setStatus(DeviceStatusEnum.BOUND.getValue());
        return toVo(device, binding, entitlement);
    }

    /**
     * 根据平台配置创建设备绑定赠送存储权益。
     *
     * @param userId 绑定用户ID
     * @param config 当前生效的平台配置
     * @param effectiveTime 权益生效时间
     * @return 待保存的设备赠送权益
     */
    private StorageEntitlementEntity createGiftEntitlement(Long userId, PlatformConfigEntity config, LocalDateTime effectiveTime) {
        ZoneId zone = UserContextHolderUtil.getZoneId();
        LocalDate localDate = effectiveTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone).toLocalDate();
        LocalDateTime localExpire = switch (config.getDeviceGiftDurationUnit()) {
            case "DAY" -> localDate.plusDays(config.getDeviceGiftDurationValue()).atStartOfDay();
            case "MONTH" -> localDate.plusMonths(config.getDeviceGiftDurationValue()).atStartOfDay();
            case "YEAR" -> localDate.plusYears(config.getDeviceGiftDurationValue()).atStartOfDay();
            default -> throw new BusinessException(ErrorCodeEnum.DEVICE_PLATFORM_CONFIG_NOT_FOUND);
        };
        LocalDateTime expireTime = localExpire.atZone(zone).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        return new StorageEntitlementEntity().setId(IdWorker.getId()).setEntitlementNo("ENT-" + IdWorker.getId())
                .setUserId(userId).setSourceType("DEVICE_GIFT").setNameSnapshot("设备绑定赠送")
                .setCapacityBytes(config.getDeviceGiftCapacityBytes()).setDurationValue(config.getDeviceGiftDurationValue())
                .setDurationUnit(config.getDeviceGiftDurationUnit()).setUserTimeZoneSnapshot(zone.getId())
                .setEffectiveTime(effectiveTime).setExpireTime(expireTime).setStatus("ACTIVE");
    }

    /**
     * 将设备、绑定和赠送权益转换为前端设备资料。
     *
     * @param device 设备实体
     * @param binding 设备绑定记录
     * @param entitlement 设备赠送权益，可为空
     * @return 设备资料响应
     */
    private DeviceVo toVo(DeviceEntity device, DeviceBindingEntity binding, StorageEntitlementEntity entitlement) {
        return new DeviceVo(device.getId(), device.getDeviceId(), device.getModel(), device.getStatus(), binding.getBindTime(),
                entitlement == null ? null : entitlement.getCapacityBytes(), entitlement == null ? null : entitlement.getExpireTime());
    }
}
