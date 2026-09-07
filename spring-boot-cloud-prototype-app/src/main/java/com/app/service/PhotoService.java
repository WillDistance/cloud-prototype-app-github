package com.app.service;

import com.app.pojo.CursorPageResult;
import com.app.pojo.dto.PhotoDto;
import com.app.pojo.vo.PhotoVo;

/**
 * 当前用户相册服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface PhotoService {
    /**
     * 按查询条件分页获取当前用户的照片列表。
     *
     * @param query 照片列表查询条件
     * @return 照片分页结果
     */
    CursorPageResult<PhotoVo.ListItem> listPhotos(PhotoDto.ListQuery query);

    /**
     * 查询当前用户的照片详情。
     *
     * @param photoNo 照片编号
     * @return 照片详情
     */
    PhotoVo.Detail getPhotoDetail(String photoNo);

    /**
     * 生成当前用户照片的原图下载地址。
     *
     * @param photoNo 照片编号
     * @return 原图下载地址
     */
    PhotoVo.DownloadUrl getOriginalDownloadUrl(String photoNo);

    /**
     * 永久删除当前用户指定的照片。
     *
     * @param request 照片删除请求
     */
    void deletePhotos(PhotoDto.DeleteRequest request);
}
