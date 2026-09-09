package com.app.service;

import com.app.pojo.dto.DeviceUploadRequest;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.DeviceUploadVo;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 设备上传业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface DeviceUploadService extends IService<PhotoFileEntity> {
    /**
     * 校验设备身份、文件信息和可用容量后创建原图记录并生成上传地址。
     *
     * @param request 设备上传请求
     * @return 原图记录及PUT预签名地址
     */
    DeviceUploadVo createUploadSession(DeviceUploadRequest request);
}
