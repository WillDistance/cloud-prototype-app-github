package com.app.service;

import com.app.pojo.dto.DeviceUploadDto;
import com.app.pojo.vo.DeviceUploadVo;

import java.time.LocalDateTime;

/**
 * 绑定设备上传会话服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface DeviceUploadService {
    /**
     * 校验设备上传请求并创建上传会话。
     *
     * @param dto 创建上传会话请求
     * @return 上传会话信息
     */
    DeviceUploadVo.CreateSession createUploadSession(DeviceUploadDto.CreateSession dto);

    /**
     * 查询设备上传会话的处理状态。
     *
     * @param dto 上传会话状态查询请求
     * @return 上传会话状态
     */
    DeviceUploadVo.SessionStatus getUploadSessionStatus(DeviceUploadDto.SessionStatus dto);

    /**
     * 释放已过期上传会话占用的存储容量。
     *
     * @param now       当前UTC时间
     * @param batchSize 本次处理的最大记录数
     * @return 实际释放的记录数
     */
    int releaseExpiredReservations(LocalDateTime now, int batchSize);
}
