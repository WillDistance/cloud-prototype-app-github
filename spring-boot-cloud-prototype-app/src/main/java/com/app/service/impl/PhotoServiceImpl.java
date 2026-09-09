package com.app.service.impl;

import com.app.mapper.PhotoFileMapper;
import com.app.pojo.entity.PhotoFileEntity;
import com.app.pojo.vo.PhotoVo;
import com.app.service.PhotoService;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 相册照片业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoFileMapper, PhotoFileEntity> implements PhotoService {
    @Autowired
    private PhotoFileMapper photoFileMapper;

    @Override
    public List<PhotoVo> listPhotos() {
        return photoFileMapper.selectAvailableByUserId(UserContextHolderUtil.getUserId()).stream()
                .map(this::toVo).toList();
    }

    /**
     * 将照片文件实体转换为相册响应对象。
     *
     * @param photo 照片文件实体
     * @return 相册照片响应
     */
    private PhotoVo toVo(PhotoFileEntity photo) {
        return new PhotoVo(photo.getId(), photo.getFileName(), photo.getFileType(), photo.getObjectKey(), photo.getUploadedTime());
    }
}
