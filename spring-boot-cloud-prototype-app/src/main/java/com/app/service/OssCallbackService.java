package com.app.service;

import com.app.pojo.dto.OssUploadCompletedRequest;
import com.app.pojo.entity.PhotoFileEntity;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 对象存储上传回调业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface OssCallbackService extends IService<PhotoFileEntity> {
    /**
     * 校验对象存储回调并将原图交给派生图处理流程。
     *
     * @param request 上传完成回调请求
     */
    void uploadCompleted(OssUploadCompletedRequest request);
}
