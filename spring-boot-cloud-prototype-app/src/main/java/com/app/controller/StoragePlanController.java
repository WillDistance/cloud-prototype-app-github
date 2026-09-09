package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.vo.StoragePlanVo;
import com.app.service.StoragePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 存储套餐控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/storagePlan")
public class StoragePlanController {
    @Autowired
    private StoragePlanService storagePlanService;

    /**
     * 查询当前可销售的存储套餐。
     *
     * @return 可销售套餐列表
     */
    @GetMapping("/listActivePlans")
    public CommonResult<List<StoragePlanVo>> listActivePlans() {
        return CommonResult.success(storagePlanService.listActivePlans());
    }
}
