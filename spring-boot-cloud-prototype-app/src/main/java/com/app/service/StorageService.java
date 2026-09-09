package com.app.service;

import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.StorageEntitlementVo;
import com.app.pojo.vo.StorageOverviewVo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 用户存储容量业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface StorageService extends IService<UserStorageAccountEntity> {
    /**
     * 查询当前用户的容量概览。
     *
     * @return 总容量、已使用容量、预留容量和剩余容量
     */
    StorageOverviewVo getOverview();

    /**
     * 查询当前用户在当前UTC时刻有效的存储权益。
     *
     * @return 有效存储权益列表
     */
    List<StorageEntitlementVo> listEntitlements();
}
