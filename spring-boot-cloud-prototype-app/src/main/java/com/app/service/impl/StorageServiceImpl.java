package com.app.service.impl;

import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.StorageVo;
import com.app.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 用户存储查询服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class StorageServiceImpl implements StorageService {
    private final StorageEntitlementMapper entitlementMapper;
    private final UserStorageAccountMapper accountMapper;
    private final Clock clock;

    @Autowired
    public StorageServiceImpl(StorageEntitlementMapper entitlementMapper, UserStorageAccountMapper accountMapper) {
        this(entitlementMapper, accountMapper, Clock.systemUTC());
    }

    public StorageServiceImpl(StorageEntitlementMapper entitlementMapper, UserStorageAccountMapper accountMapper, Clock clock) {
        this.entitlementMapper = entitlementMapper;
        this.accountMapper = accountMapper;
        this.clock = clock;
    }

    @Override
    public StorageVo.Overview getOverview(Long userId) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        List<StorageEntitlementEntity> entitlements = entitlementMapper.selectEffectiveByUserId(userId, now);
        long total = entitlements.stream().map(StorageEntitlementEntity::getCapacityBytes)
                .filter(java.util.Objects::nonNull).mapToLong(Long::longValue).sum();
        UserStorageAccountEntity account = accountMapper.selectByUserId(userId);
        long used = account == null || account.getUsedBytes() == null ? 0L : account.getUsedBytes();
        long reserved = account == null || account.getReservedBytes() == null ? 0L : account.getReservedBytes();
        return new StorageVo.Overview().setTotalCapacityBytes(total).setUsedBytes(used).setReservedBytes(reserved)
                .setRemainingBytes(Math.max(total - used - reserved, 0L))
                .setNearestExpireTime(entitlements.isEmpty() ? null : entitlements.getFirst().getExpireTime());
    }

    @Override
    public List<StorageVo.Entitlement> listEntitlements(Long userId) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        return entitlementMapper.selectEffectiveByUserId(userId, now).stream().map(this::toVo).toList();
    }

    /**
     * 将业务实体转换为前端视图对象。
     *
     * @param entity 数据库实体
     * @return 方法处理后的结果
     */
    private StorageVo.Entitlement toVo(StorageEntitlementEntity entity) {
        return new StorageVo.Entitlement().setEntitlementNo(entity.getEntitlementNo())
                .setSourceType(entity.getSourceType().getValue()).setNameSnapshot(entity.getNameSnapshot())
                .setCapacityBytes(entity.getCapacityBytes()).setEffectiveTime(entity.getEffectiveTime())
                .setExpireTime(entity.getExpireTime());
    }
}
