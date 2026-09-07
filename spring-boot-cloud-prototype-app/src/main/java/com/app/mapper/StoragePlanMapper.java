package com.app.mapper;

import com.app.pojo.entity.StoragePlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 存储套餐数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface StoragePlanMapper extends BaseMapper<StoragePlanEntity> {
    /**
     * 查询指定版本且在指定时刻已生效的在售套餐
     *
     * @param planCode    套餐编码
     * @param planVersion 套餐版本
     * @param now         当前UTC时间
     * @return 生效套餐，不存在时返回null
     */
    StoragePlanEntity selectActivePlan(@Param("planCode") String planCode,
                                       @Param("planVersion") Integer planVersion,
                                       @Param("now") LocalDateTime now);

    /**
     * 查询指定时刻已生效且在售的套餐
     *
     * @param now 当前UTC时间
     * @return 生效套餐列表
     */
    List<StoragePlanEntity> selectActivePlans(@Param("now") LocalDateTime now);
}
