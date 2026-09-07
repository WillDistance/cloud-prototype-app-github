package com.app.service;

import com.app.pojo.dto.OssCallbackDto;
import com.app.pojo.vo.OssCallbackVo;

import java.time.LocalDateTime;

/**
 * OSS回调与照片发布服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface OssCallbackService {
    /**
     * 处理对象存储上传完成回调并发布照片。
     *
     * @param dto 对象存储回调请求
     * @return 照片发布结果
     */
    OssCallbackVo uploadCompleted(OssCallbackDto dto);

    /**
     * 清理没有对应业务记录的对象存储文件。
     *
     * @param now       当前UTC时间
     * @param batchSize 本次处理的最大记录数
     * @return 实际清理的对象数量
     */
    int cleanupOrphanObjects(LocalDateTime now, int batchSize);
}
