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

    /**
     * 按用户、UTC时间范围和游标查询可访问原图。
     *
     * @param userId 用户ID
     * @param startTime UTC起始时间，可为空
     * @param endTime UTC结束时间，可为空
     * @param cursorTime 上一页游标时间，可为空
     * @param cursorId 上一页游标ID，可为空
     * @param limit 查询数量
     * @return 按上传时间倒序排列的照片列表
     */
    List<PhotoFileEntity> selectAvailableByCursor(@Param("userId") Long userId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("cursorTime") LocalDateTime cursorTime,
                                                  @Param("cursorId") Long cursorId,
                                                  @Param("limit") Integer limit);

    /**
     * 查询用户按上传时间升序排列的可访问原图。
     *
     * @param userId 用户ID
     * @return 用户可访问原图列表
     */
    List<PhotoFileEntity> selectAvailableOriginals(@Param("userId") Long userId);


    /**
     * 更新照片删除状态和删除原因。
     *
     * @param id 文件记录主键ID
     * @param status 删除状态
     * @param reason 删除原因
     * @return 受影响的记录数
     */
    int updateDeleteStatus(@Param("id") Long id, @Param("status") String status, @Param("reason") String reason);

    /**
     * 查询用户所有处于指定删除状态的照片文件。
     *
     * @param userId 用户ID
     * @param status 文件状态
     * @return 待重试照片文件列表
     */
    List<PhotoFileEntity> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询处于指定删除状态的照片文件。
     *
     * @param status 文件删除状态
     * @return 待处理照片文件列表
     */
    List<PhotoFileEntity> selectByStatus(@Param("status") String status);
}
