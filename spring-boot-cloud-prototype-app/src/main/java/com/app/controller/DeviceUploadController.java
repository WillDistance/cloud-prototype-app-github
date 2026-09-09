package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.DeviceUploadRequest;
import com.app.pojo.vo.DeviceUploadVo;
import com.app.service.DeviceUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备上传控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/deviceUpload")
public class DeviceUploadController {
    @Autowired
    private DeviceUploadService deviceUploadService;

    /**
     * 创建原图上传记录并生成MinIO PUT预签名地址。
     *
     * @param request 设备上传请求
     * @return 上传地址及原图记录信息
     */
    @PostMapping("/createUploadSession")
    public CommonResult<DeviceUploadVo> createUploadSession(@Valid @RequestBody DeviceUploadRequest request) {
        return CommonResult.success(deviceUploadService.createUploadSession(request));
    }
}
