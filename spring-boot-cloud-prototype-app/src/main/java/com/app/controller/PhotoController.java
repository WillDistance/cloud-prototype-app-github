package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.DeletePhotosRequest;
import com.app.pojo.dto.PhotoDetailRequest;
import com.app.pojo.dto.PhotoListRequest;
import com.app.pojo.vo.PhotoDetailVo;
import com.app.pojo.vo.PhotoDownloadVo;
import com.app.pojo.vo.PhotoVo;
import com.app.pojo.vo.PhotoPageVo;
import com.app.service.PhotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
     * @param request 时间范围和游标分页参数
     * @return 可访问照片分页结果
     */
    @GetMapping("/listPhotos")
    public CommonResult<PhotoPageVo> listPhotos(@Valid PhotoListRequest request) {
        return CommonResult.success(photoService.listPhotos(request));
    }

    /**
     * 查询照片详情并返回预览图地址。
     *
     * @param request 照片详情请求
     * @return 照片详情
     */
    @GetMapping("/getPhotoDetail")
    public CommonResult<PhotoDetailVo> getPhotoDetail(@Valid PhotoDetailRequest request) {
        return CommonResult.success(photoService.getPhotoDetail(request));
    }

    /**
     * 生成当前用户原图临时下载地址。
     *
     * @param request 照片详情请求
     * @return 原图下载地址
     */
    @GetMapping("/getOriginalDownloadUrl")
    public CommonResult<PhotoDownloadVo> getOriginalDownloadUrl(@Valid PhotoDetailRequest request) {
        return CommonResult.success(photoService.getOriginalDownloadUrl(request));
    }

    /**
     * 批量删除当前用户照片。
     *
     * @param request 批量删除请求
     * @return 空数据成功响应
     */
    @PostMapping("/deletePhotos")
    public CommonResult<Void> deletePhotos(@Valid @RequestBody DeletePhotosRequest request) {
        photoService.deletePhotos(request);
        return CommonResult.success(null);
    }
}
