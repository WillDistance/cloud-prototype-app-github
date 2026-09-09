package com.app.service;

import com.app.pojo.dto.DeviceBindRequest;
import com.app.pojo.entity.DeviceEntity;
import com.app.pojo.vo.DeviceVo;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 设备绑定业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface DeviceService extends IService<DeviceEntity> {
    /**
     * 查询当前用户已经绑定的设备。
     *
     * @return 当前用户设备资料，未绑定时返回null
     */
    DeviceVo getMyDevice();

    /**
     * 使用设备编号和初始密码完成用户的永久绑定。
     *
     * @param request 设备绑定请求
     * @return 绑定后的设备资料
     */
    DeviceVo bindDevice(DeviceBindRequest request);
}
