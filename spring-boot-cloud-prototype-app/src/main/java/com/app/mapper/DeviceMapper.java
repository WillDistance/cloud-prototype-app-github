package com.app.mapper;

import com.app.enums.DeviceStatusEnum;
import com.app.pojo.entity.DeviceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * DeviceEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface DeviceMapper extends BaseMapper<DeviceEntity> {
    /**
     * 按业务设备编号查询设备记录。
     *
     * @param deviceId 设备ID
     * @return 查询结果
     */
    DeviceEntity selectByBusinessDeviceId(@Param("deviceId") String deviceId);

    /**
     * 按业务设备编号查询并锁定设备记录。
     *
     * @param deviceId 设备ID
     * @return 查询结果
     */
    DeviceEntity selectByDeviceIdForUpdate(@Param("deviceId") String deviceId);

    /**
     * 设备未绑定时，原子更新设备状态。
     *
     * @param id 记录ID
     * @param expectedStatus 期望的当前设备状态
     * @param targetStatus 目标设备状态
     * @return 受影响的记录数
     */
    int markBoundIfUnbound(@Param("id") Long id,
                           @Param("expectedStatus") DeviceStatusEnum expectedStatus,
                           @Param("targetStatus") DeviceStatusEnum targetStatus);
}
