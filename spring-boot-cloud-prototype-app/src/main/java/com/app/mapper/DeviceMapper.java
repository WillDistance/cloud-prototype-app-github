package com.app.mapper;

import com.app.pojo.entity.DeviceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_device表数据访问接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface DeviceMapper extends BaseMapper<DeviceEntity> {
    /**
     * 按主键查询并锁定设备记录，供并发绑定事务校验当前状态。
     *
     * @param id 设备记录主键ID
     * @return 被锁定的设备记录，不存在时返回null
     */
    DeviceEntity selectByIdForUpdate(Long id);

    /**
     * 根据设备业务编号查询设备记录。
     *
     * @param deviceId 设备业务编号
     * @return 设备记录，不存在时返回null
     */
    DeviceEntity selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 在设备当前状态符合预期时原子更新设备状态。
     *
     * @param id 设备记录主键ID
     * @param status 要更新成的设备状态
     * @param expectedStatus 允许执行更新的当前设备状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);
}
