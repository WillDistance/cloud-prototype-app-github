package com.app.mapper;

import com.app.pojo.entity.StorageEntitlementEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 存储权益数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface StorageEntitlementMapper extends BaseMapper<StorageEntitlementEntity> {
    /**
     * 查询待处理的存储权益到期记录。
     *
     * @param now 当前UTC时间
     * @param limit 查询数量上限
     * @return 待处理的存储权益列表
     */
    List<StorageEntitlementEntity> selectPendingExpiration(@Param("now") LocalDateTime now, @Param("limit") int limit);

    /**
     * 查询用户在指定时刻生效的存储权益。
     *
     * @param userId 用户ID
     * @param now 当前UTC时间
     * @return 有效存储权益列表
     */
    List<StorageEntitlementEntity> selectEffectiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 汇总用户在指定时刻生效的存储容量。
     *
     * @param userId 用户ID
     * @param now 当前UTC时间
     * @return 有效存储容量，未查询到时返回null
     */
    Long sumActiveCapacity(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 将存储权益标记为已完成到期处理。
     *
     * @param id 记录ID
     * @param processedTime 处理时间
     * @return 受影响的记录数
     */
    int markExpiredProcessed(@Param("id") Long id, @Param("processedTime") LocalDateTime processedTime);
}
