package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.CursorPageResult;
import com.app.pojo.dto.PhotoDto;
import com.app.pojo.vo.PhotoVo;
import com.app.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * 相册查询控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/photo")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    /**
     * 按时间范围游标分页查询相册
     *
     * @param filter     时间范围
     * @param cursorTime 游标上传时间
     * @param cursorId   游标照片ID
     * @param size       每页数量
     * @return 相册分页结果
     */
    @GetMapping("/listPhotos")
    public CommonResult<CursorPageResult<PhotoVo.ListItem>> listPhotos(
            @RequestParam(name = "filter", defaultValue = "ALL") String filter,
            @RequestParam(name = "cursorTime", required = false) Instant cursorTime,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return CommonResult.success(photoService.listPhotos(new PhotoDto.ListQuery().setFilter(filter)
                .setCursorTime(cursorTime).setCursorId(cursorId).setSize(size)));
    }

    /**
     * 查询照片详情
     *
     * @param photoNo 照片业务编号
     * @return 照片详情
     */
    @GetMapping("/getPhotoDetail")
    public CommonResult<PhotoVo.Detail> getPhotoDetail(@RequestParam("photoNo") String photoNo) {
        return CommonResult.success(photoService.getPhotoDetail(photoNo));
    }

    /**
     * 获取原图短时下载地址
     *
     * @param photoNo 照片业务编号
     * @return 原图下载地址
     */
    @GetMapping("/getOriginalDownloadUrl")
    public CommonResult<PhotoVo.DownloadUrl> getOriginalDownloadUrl(@RequestParam("photoNo") String photoNo) {
        return CommonResult.success(photoService.getOriginalDownloadUrl(photoNo));
    }

    /**
     * 批量永久删除当前用户照片。
     *
     * @param request 删除请求
     * @return 空结果
     */
    @PostMapping("/deletePhotos")
    public CommonResult<Void> deletePhotos(@RequestBody PhotoDto.DeleteRequest request) {
        photoService.deletePhotos(request);
        return CommonResult.success(null);
    }
}
