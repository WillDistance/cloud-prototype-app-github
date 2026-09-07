package com.app.service;

import com.app.pojo.vo.StorageVo;

import java.util.List;

/**
 * 用户存储查询服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface StorageService {
    /**
     * 查询当前用户的存储容量概览。
     *
     * @param userId 用户ID
     * @return 存储容量概览
     */
    StorageVo.Overview getOverview(Long userId);

    /**
     * 查询当前用户的有效存储权益。
     *
     * @param userId 用户ID
     * @return 有效存储权益列表
     */
    List<StorageVo.Entitlement> listEntitlements(Long userId);
}
