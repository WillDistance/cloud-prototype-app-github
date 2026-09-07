package com.app.service;

import com.app.pojo.dto.DeviceDto;
import com.app.pojo.vo.DeviceVo;

/**
 * 设备永久绑定服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface DeviceService {
    /**
     * 查询当前用户绑定的设备信息。
     *
     * @param userId 用户ID
     * @return 当前用户的设备信息
     */
    DeviceVo.MyDevice getMyDevice(Long userId);

    /**
     * 校验设备凭证并建立设备绑定关系。
     *
     * @param userId 用户ID
     * @param dto    设备绑定请求
     * @return 设备绑定结果
     */
    DeviceVo.BindResult bindDevice(Long userId, DeviceDto.BindDevice dto);
}
