package com.app.service;

import com.app.pojo.dto.DeletePhotosRequest;
import com.app.pojo.dto.PhotoDetailRequest;
import com.app.pojo.dto.PhotoListRequest;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.PhotoDetailVo;
import com.app.pojo.vo.PhotoDownloadVo;
import com.app.pojo.vo.PhotoPageVo;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 相册照片业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface PhotoService extends IService<PhotoFileEntity> {
    /**
     * 按时间范围和游标查询当前用户已经发布的照片。
     *
     * @param request 相册筛选和游标分页请求
     * @return 相册游标分页结果
     */
    PhotoPageVo listPhotos(PhotoListRequest request);

    /**
     * 查询当前用户照片详情并生成预览地址。
     *
     * @param request 照片详情请求
     * @return 照片详情，其中预览图地址为对象存储短期预签名URL
     */
    PhotoDetailVo getPhotoDetail(PhotoDetailRequest request);

    /**
     * 校验照片归属和状态并生成原图下载地址。
     *
     * @param request 照片详情请求
     * @return 原图临时下载地址，实际指向对象存储预签名URL
     */
    PhotoDownloadVo getOriginalDownloadUrl(PhotoDetailRequest request);

    /**
     * 将当前用户指定的原图记录标记为待删除，由定时任务删除对象存储文件并释放容量。
     *
     * @param request 批量删除请求
     */
    void deletePhotos(DeletePhotosRequest request);
}
