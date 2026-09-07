package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.DeviceUploadDto;
import com.app.pojo.vo.DeviceUploadVo;
import com.app.service.DeviceUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设备上传控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/deviceUpload")
public class DeviceUploadController {

    @Autowired
    private DeviceUploadService deviceUploadService;

    /**
     * 创建设备上传会话
     *
     * @param dto 上传会话参数
     * @return 上传会话信息
     */
    @PostMapping("/createUploadSession")
    public CommonResult<DeviceUploadVo.CreateSession> createUploadSession(
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-Device-Password") String password,
            @Valid @RequestBody DeviceUploadDto.CreateSession dto) {
        dto.setDeviceId(deviceId).setPassword(password);
        return CommonResult.success(deviceUploadService.createUploadSession(dto));
    }

    /**
     * 查询设备上传会话状态
     *
     * @param uploadNo 上传会话编号
     * @return 上传会话状态
     */
    @GetMapping("/getUploadSessionStatus")
    public CommonResult<DeviceUploadVo.SessionStatus> getUploadSessionStatus(
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-Device-Password") String password,
            @RequestParam("uploadNo") String uploadNo) {
        DeviceUploadDto.SessionStatus dto = new DeviceUploadDto.SessionStatus()
                .setDeviceId(deviceId).setPassword(password).setUploadNo(uploadNo);
        return CommonResult.success(deviceUploadService.getUploadSessionStatus(dto));
    }
}
