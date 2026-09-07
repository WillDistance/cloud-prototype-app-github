package com.app.mapper;

import com.app.pojo.entity.PhotoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PhotoEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface PhotoMapper extends BaseMapper<PhotoEntity> {
    /**
     * 按上传会话ID查询对应的照片记录。
     *
     * @param uploadSessionId 上传会话ID
     * @return 查询结果
     */
    PhotoEntity selectByUploadSessionId(@Param("uploadSessionId") Long uploadSessionId);
}
