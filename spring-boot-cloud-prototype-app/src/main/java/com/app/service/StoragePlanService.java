package com.app.service;

import com.app.pojo.entity.StoragePlanEntity;
import com.app.pojo.vo.StoragePlanVo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 存储套餐业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface StoragePlanService extends IService<StoragePlanEntity> {
    /**
     * 查询当前可销售的存储套餐。
     *
     * @return 按展示顺序排列的套餐列表
     */
    List<StoragePlanVo> listActivePlans();
}
