package com.app.mapper;

import com.app.pojo.entity.UploadSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UploadSessionEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface UploadSessionMapper extends BaseMapper<UploadSessionEntity> {
    /**
     * 按上传单号查询并锁定上传会话记录。
     *
     * @param uploadNo 上传单号
     * @return 上传会话记录
     */
    UploadSessionEntity selectByUploadNoForUpdate(@Param("uploadNo") String uploadNo);

    /**
     * 按上传单号和设备ID查询上传会话记录。
     *
     * @param uploadNo 上传单号
     * @param deviceId 设备ID
     * @return 上传会话记录
     */
    UploadSessionEntity selectByUploadNoAndDeviceId(@Param("uploadNo") String uploadNo, @Param("deviceId") Long deviceId);

    /**
     * 按上传单号查询上传会话记录。
     *
     * @param uploadNo 上传单号
     * @return 上传会话记录
     */
    UploadSessionEntity selectByUploadNo(@Param("uploadNo") String uploadNo);

    /**
     * 查询已过期且需要锁定处理的上传会话。
     *
     * @param now 当前UTC时间
     * @param batchSize 批处理数量
     * @return 待处理的上传会话列表
     */
    List<UploadSessionEntity> selectExpiredForUpdate(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);

    /**
     * 上传地址已签发时，将上传会话标记为已过期。
     *
     * @param id 记录ID
     * @return 受影响的记录数
     */
    int markExpiredIfUrlIssued(@Param("id") Long id);

    /**
     * 上传地址已签发时，记录原图已上传事件。
     *
     * @param id 记录ID
     * @param eventId 对象存储事件ID
     * @return 受影响的记录数
     */
    int markOriginalUploadedIfUrlIssued(@Param("id") Long id, @Param("eventId") String eventId);

    /**
     * 将已上传或可重试失败的会话标记为处理中。
     *
     * @param id 记录ID
     * @return 受影响的记录数
     */
    int markProcessingIfUploadedOrRetryableFailed(@Param("id") Long id);

    /**
     * 处理中会话完成后，更新会话完成状态。
     *
     * @param id 记录ID
     * @param completedTime 完成时间
     * @return 受影响的记录数
     */
    int markCompletedIfProcessing(@Param("id") Long id, @Param("completedTime") LocalDateTime completedTime);

    /**
     * 将失败会话标记为失败并释放预留容量。
     *
     * @param id 记录ID
     * @param failureCode 失败编码
     * @param failureReason 失败原因
     * @return 受影响的记录数
     */
    int markFailedAndReleaseIfReserved(@Param("id") Long id, @Param("failureCode") String failureCode,
                                       @Param("failureReason") String failureReason);

    /**
     * 将处理中会话标记为可重试失败。
     *
     * @param id 记录ID
     * @param failureCode 失败编码
     * @param failureReason 失败原因
     * @return 受影响的记录数
     */
    int markRetryableFailedIfProcessing(@Param("id") Long id, @Param("failureCode") String failureCode,
                                        @Param("failureReason") String failureReason);

    /**
     * 查询失败且需要清理预留容量的上传会话。
     *
     * @param now 当前UTC时间
     * @param batchSize 批处理数量
     * @return 待清理的上传会话列表
     */
    List<UploadSessionEntity> selectFailedForCleanup(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);

    /**
     * 失败会话完成清理后，标记预留容量已释放。
     *
     * @param id 记录ID
     * @return 受影响的记录数
     */
    int markCleanupReleasedIfFailed(@Param("id") Long id);
}
