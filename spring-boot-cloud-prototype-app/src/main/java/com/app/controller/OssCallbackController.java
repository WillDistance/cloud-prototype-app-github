package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.OssUploadCompletedRequest;
import com.app.service.OssCallbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OSS回调控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/ossCallback")
public class OssCallbackController {
    @Autowired
    private OssCallbackService ossCallbackService;

    /**
     * 接收对象存储上传完成通知。
     *
     * @param request 上传完成回调请求
     * @return 空数据成功响应
     */
    @PostMapping("/uploadCompleted")
    public CommonResult<Void> uploadCompleted(@Valid @RequestBody OssUploadCompletedRequest request) {
        ossCallbackService.uploadCompleted(request);
        return CommonResult.success(null);
    }
}
