package com.app.service.impl;

import com.app.mapper.StoragePlanMapper;
import com.app.pojo.entity.StoragePlanEntity;
import com.app.pojo.vo.StoragePlanVo;
import com.app.service.StoragePlanService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 存储套餐业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class StoragePlanServiceImpl extends ServiceImpl<StoragePlanMapper, StoragePlanEntity> implements StoragePlanService {
    @Autowired
    private StoragePlanMapper storagePlanMapper;

    @Override
    public List<StoragePlanVo> listActivePlans() {
        return storagePlanMapper.selectActivePlans().stream().map(this::toVo).toList();
    }

    /**
     * 将存储套餐实体转换为前端响应对象。
     *
     * @param entity 存储套餐实体
     * @return 存储套餐响应
     */
    private StoragePlanVo toVo(StoragePlanEntity entity) {
        return new StoragePlanVo(entity.getPlanCode(), entity.getPlanVersion(), entity.getPlanName(), entity.getCapacityBytes(),
                entity.getDurationValue(), entity.getDurationUnit(), entity.getPriceCent(), entity.getCurrency(), entity.getRecommended() != null && entity.getRecommended() == 1);
    }
}
