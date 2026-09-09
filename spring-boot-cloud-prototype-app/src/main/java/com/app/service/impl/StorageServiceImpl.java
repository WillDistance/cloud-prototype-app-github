package com.app.service.impl;

import com.app.enums.EntitlementStatusEnum;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.StorageEntitlementVo;
import com.app.pojo.vo.StorageOverviewVo;
import com.app.service.StorageService;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户存储容量业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class StorageServiceImpl extends ServiceImpl<UserStorageAccountMapper, UserStorageAccountEntity> implements StorageService {
    @Autowired
    private UserStorageAccountMapper storageAccountMapper;
    @Autowired
    private StorageEntitlementMapper entitlementMapper;

    @Override
    public StorageOverviewVo getOverview() {
        UserStorageAccountEntity account = storageAccountMapper.selectByUserId(UserContextHolderUtil.getUserId());
        long used = account == null || account.getUsedBytes() == null ? 0L : account.getUsedBytes();
        long reserved = account == null || account.getReservedBytes() == null ? 0L : account.getReservedBytes();
        long total = entitlementMapper.selectActiveByUserId(UserContextHolderUtil.getUserId(), LocalDateTime.now()).stream()
                .mapToLong(StorageEntitlementEntity::getCapacityBytes).sum();
        return new StorageOverviewVo(total, used, reserved, Math.max(0L, total - used - reserved));
    }

    @Override
    public List<StorageEntitlementVo> listEntitlements() {
        return entitlementMapper.selectActiveByUserId(UserContextHolderUtil.getUserId(), LocalDateTime.now()).stream()
                .map(this::toVo).toList();
    }

    /**
     * 将存储权益实体转换为前端响应对象。
     *
     * @param entity 存储权益实体
     * @return 存储权益响应
     */
    private StorageEntitlementVo toVo(StorageEntitlementEntity entity) {
        return new StorageEntitlementVo(entity.getEntitlementNo(), entity.getSourceType(), entity.getNameSnapshot(),
                entity.getCapacityBytes(), entity.getEffectiveTime(), entity.getExpireTime(), EntitlementStatusEnum.ACTIVE.getValue());
    }
}
