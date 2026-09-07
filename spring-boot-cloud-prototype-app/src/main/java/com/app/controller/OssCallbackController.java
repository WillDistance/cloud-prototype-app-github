package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.OssCallbackDto;
import com.app.pojo.vo.OssCallbackVo;
import com.app.service.OssCallbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OSS回调控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/ossCallback")
public class OssCallbackController {
    @Autowired
    private OssCallbackService ossCallbackService;

    /**
     * 处理上传完成回调
     *
     * @param dto OSS回调参数
     * @return 照片发布结果
     */
    @PostMapping("/uploadCompleted")
    public CommonResult<OssCallbackVo> uploadCompleted(@Valid @RequestBody OssCallbackDto dto) {
        return CommonResult.success(ossCallbackService.uploadCompleted(dto));
    }
}
