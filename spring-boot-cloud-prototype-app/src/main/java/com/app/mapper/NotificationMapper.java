package com.app.mapper;

import com.app.pojo.entity.NotificationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * t_notification表数据访问接口
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity> {
    /**
     * 按主键查询并锁定通知记录。
     *
     * @param id 通知记录主键ID
     * @return 被锁定的通知记录，不存在时返回null
     */
    NotificationEntity selectByIdForUpdate(Long id);
    /**
     * 在通知当前状态符合预期时原子更新通知状态。
     *
     * @param id 通知记录主键ID
     * @param status 要更新成的通知状态
     * @param expectedStatus 允许执行更新的当前通知状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    /**
     * 查询当前可以发送的待处理通知。
     *
     * @param now 当前UTC时间
     * @return 待发送通知列表
     */
    List<NotificationEntity> selectPending(@Param("now") LocalDateTime now);

    /**
     * 将通知标记为发送成功。
     *
     * @param id 通知主键ID
     * @param sentTime 发送成功时间
     * @return 受影响的记录数
     */
    int markSent(@Param("id") Long id, @Param("sentTime") LocalDateTime sentTime);

    /**
     * 记录通知发送失败并安排下一次重试。
     *
     * @param id 通知主键ID
     * @param reason 失败原因
     * @param nextRetryTime 下一次重试时间
     * @return 受影响的记录数
     */
    int markFailed(@Param("id") Long id, @Param("reason") String reason, @Param("nextRetryTime") LocalDateTime nextRetryTime);
}
