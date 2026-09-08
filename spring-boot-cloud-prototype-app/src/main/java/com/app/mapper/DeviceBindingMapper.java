package com.app.mapper;

import com.app.pojo.entity.DeviceBindingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_device_binding表数据访问接口
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface DeviceBindingMapper extends BaseMapper<DeviceBindingEntity> {
    /**
     * 按主键查询并锁定数据库记录。
     *
     * @param id 设备绑定记录主键ID
     * @return 查询结果
     */
    DeviceBindingEntity selectByIdForUpdate(Long id);
    /**
     * 在期望状态匹配时原子更新记录状态。
     *
     * @param id 数据库记录主键ID
     * @param status 目标状态
     * @param expectedStatus 期望的当前状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);
}
