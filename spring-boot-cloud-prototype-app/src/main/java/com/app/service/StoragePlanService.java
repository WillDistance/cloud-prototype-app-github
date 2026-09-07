package com.app.service;

import com.app.pojo.vo.StoragePlanVo;

import java.util.List;

/**
 * 存储套餐查询服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface StoragePlanService {
    /**
     * 查询当前生效的存储套餐列表。
     *
     * @return 生效的存储套餐列表
     */
    List<StoragePlanVo.ActivePlan> listActivePlans();
}
