package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.DeviceBindRequest;
import com.app.pojo.vo.DeviceVo;
import com.app.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备绑定控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    /**
     * 查询当前登录用户绑定的设备。
     *
     * @return 当前设备资料，未绑定时返回null
     */
    @GetMapping("/getMyDevice")
    public CommonResult<DeviceVo> getMyDevice() {
        return CommonResult.success(deviceService.getMyDevice());
    }

    /**
     * 绑定设备并创建永久绑定记录。
     *
     * @param request 设备编号和初始密码
     * @return 绑定后的设备资料
     */
    @PostMapping("/bindDevice")
    public CommonResult<DeviceVo> bindDevice(@Valid @RequestBody DeviceBindRequest request) {
        return CommonResult.success(deviceService.bindDevice(request));
    }
}
