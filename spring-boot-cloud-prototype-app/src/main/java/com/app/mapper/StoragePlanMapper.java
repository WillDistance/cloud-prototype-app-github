package com.app.mapper;

import com.app.pojo.entity.StoragePlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * t_storage_plan表数据访问接口
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface StoragePlanMapper extends BaseMapper<StoragePlanEntity> {
    /**
     * 按主键查询并锁定存储套餐记录。
     *
     * @param id 存储套餐记录主键ID
     * @return 被锁定的存储套餐，不存在时返回null
     */
    StoragePlanEntity selectByIdForUpdate(Long id);
    /**
     * 在存储套餐当前状态符合预期时原子更新套餐状态。
     *
     * @param id 存储套餐记录主键ID
     * @param status 要更新成的套餐状态
     * @param expectedStatus 允许执行更新的当前套餐状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    /**
     * 查询当前可销售的存储套餐并按展示顺序排列。
     *
     * @return 可销售套餐列表
     */
    List<StoragePlanEntity> selectActivePlans();
}
