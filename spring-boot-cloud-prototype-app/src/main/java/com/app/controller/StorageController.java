package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.vo.StorageVo;
import com.app.security.UserContextHolder;
import com.app.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户存储控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    @Autowired
    private StorageService storageService;

    /**
     * 查询当前用户存储容量概览
     *
     * @return 存储容量概览
     */
    @GetMapping("/getOverview")
    public CommonResult<StorageVo.Overview> getOverview() {
        return CommonResult.success(storageService.getOverview(UserContextHolder.get().userId()));
    }

    /**
     * 查询当前用户有效权益
     *
     * @return 有效权益列表
     */
    @GetMapping("/listEntitlements")
    public CommonResult<List<StorageVo.Entitlement>> listEntitlements() {
        return CommonResult.success(storageService.listEntitlements(UserContextHolder.get().userId()));
    }
}
