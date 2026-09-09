package com.app.mapper;

import com.app.pojo.entity.StorageEntitlementEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * t_storage_entitlement表数据访问接口
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface StorageEntitlementMapper extends BaseMapper<StorageEntitlementEntity> {
    /**
     * 按主键查询并锁定存储权益记录。
     *
     * @param id 存储权益记录主键ID
     * @return 被锁定的存储权益，不存在时返回null
     */
    StorageEntitlementEntity selectByIdForUpdate(Long id);
    /**
     * 在存储权益当前状态符合预期时原子更新权益状态。
     *
     * @param id 存储权益记录主键ID
     * @param status 要更新成的权益状态
     * @param expectedStatus 允许执行更新的当前权益状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    /**
     * 查询用户在指定时刻有效的存储权益。
     *
     * @param userId 用户ID
     * @param now 当前UTC时间
     * @return 当前有效权益列表
     */
    List<StorageEntitlementEntity> selectActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 查询已到期且尚未完成处理的权益。
     *
     * @param now 当前UTC时间
     * @return 待处理到期权益列表
     */
    List<StorageEntitlementEntity> selectExpired(@Param("now") LocalDateTime now);

    /**
     * 原子将权益标记为已到期。
     *
     * @param id 权益主键ID
     * @param status 到期状态值
     * @return 受影响的记录数
     */
    int markExpired(@Param("id") Long id, @Param("status") String status);

    /**
     * 汇总用户指定时刻有效权益容量。
     *
     * @param userId 用户ID
     * @param now 当前UTC时间
     * @return 有效权益总容量，单位为字节
     */
    Long sumActiveCapacity(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 标记权益到期清理已完成。
     *
     * @param id 权益主键ID
     * @param processedTime 清理完成时间
     * @return 受影响的记录数
     */
    int markProcessed(@Param("id") Long id, @Param("processedTime") LocalDateTime processedTime);
}
