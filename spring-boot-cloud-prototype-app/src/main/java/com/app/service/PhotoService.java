package com.app.service;

import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.PhotoVo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 相册照片业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface PhotoService extends IService<PhotoFileEntity> {
    /**
     * 查询当前用户可以访问的照片文件。
     *
     * @return 当前用户的可访问照片列表
     */
    List<PhotoVo> listPhotos();
}
