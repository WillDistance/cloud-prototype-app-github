package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.vo.StorageEntitlementVo;
import com.app.pojo.vo.StorageOverviewVo;
import com.app.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户存储控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    @Autowired
    private StorageService storageService;

    /**
     * 查询当前用户的存储容量概览。
     *
     * @return 存储容量概览
     */
    @GetMapping("/getOverview")
    public CommonResult<StorageOverviewVo> getOverview() {
        return CommonResult.success(storageService.getOverview());
    }

    /**
     * 查询当前用户当前有效的存储权益。
     *
     * @return 有效存储权益列表
     */
    @GetMapping("/listEntitlements")
    public CommonResult<List<StorageEntitlementVo>> listEntitlements() {
        return CommonResult.success(storageService.listEntitlements());
    }
}
