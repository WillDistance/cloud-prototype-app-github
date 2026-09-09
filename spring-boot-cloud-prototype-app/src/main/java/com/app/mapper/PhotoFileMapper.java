package com.app.mapper;

import com.app.pojo.entity.PhotoFileEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * t_photo_file表数据访问接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface PhotoFileMapper extends BaseMapper<PhotoFileEntity> {

    /**
     * 查询指定状态的原图上传记录。
     *
     * @param status 原图上传状态
     * @return 待扫描的原图记录
     */
    List<PhotoFileEntity> selectPendingUploads(@Param("status") String status);

    /**
     * 查询指定状态且已超过清理时间的原图记录。
     *
     * @param status 上传记录状态
     * @param expireTime 清理时间边界
     * @return 待清理原图记录列表
     */
    List<PhotoFileEntity> selectExpiredUploads(@Param("status") String status, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 在原图仍处于指定状态时原子更新文件状态。
     *
     * @param id 原图记录主键ID
     * @param status 目标状态
     * @param expectedStatus 允许更新的当前状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    /**
     * 查询指定用户的可访问照片文件。
     *
     * @param userId 用户ID
     * @return 可访问照片文件列表
     */
    List<PhotoFileEntity> selectAvailableByUserId(@Param("userId") Long userId);
}
