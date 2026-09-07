package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.DeviceDto;
import com.app.pojo.vo.DeviceVo;
import com.app.security.UserContextHolder;
import com.app.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设备控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 查询当前用户绑定设备
     *
     * @return 当前用户绑定设备
     */
    @GetMapping("/getMyDevice")
    public CommonResult<DeviceVo.MyDevice> getMyDevice() {
        return CommonResult.success(deviceService.getMyDevice(UserContextHolder.get().userId()));
    }

    /**
     * 永久绑定设备
     *
     * @param dto 设备绑定参数
     * @return 设备绑定结果
     */
    @PostMapping("/bindDevice")
    public CommonResult<DeviceVo.BindResult> bindDevice(@Valid @RequestBody DeviceDto.BindDevice dto) {
        return CommonResult.success(deviceService.bindDevice(UserContextHolder.get().userId(), dto));
    }
}
