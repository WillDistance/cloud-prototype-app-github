package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.vo.PhotoVo;
import com.app.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 相册查询控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/photo")
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    /**
     * 查询当前用户已经发布的照片。
     *
     * @return 可访问照片列表
     */
    @GetMapping("/listPhotos")
    public CommonResult<List<PhotoVo>> listPhotos() {
        return CommonResult.success(photoService.listPhotos());
    }
}
