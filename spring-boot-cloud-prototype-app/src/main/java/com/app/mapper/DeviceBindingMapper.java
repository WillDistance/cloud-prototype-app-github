package com.app.mapper;

import com.app.pojo.entity.DeviceBindingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_device_binding表数据访问接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface DeviceBindingMapper extends BaseMapper<DeviceBindingEntity> {
    /**
     * 按主键查询并锁定设备绑定记录。
     *
     * @param id 设备绑定记录主键ID
     * @return 被锁定的绑定记录，不存在时返回null
     */
    DeviceBindingEntity selectByIdForUpdate(Long id);

    /**
     * 根据用户ID查询其绑定记录。
     *
     * @param userId 用户ID
     * @return 用户绑定记录，不存在时返回null
     */
    DeviceBindingEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 根据设备主键查询永久绑定关系。
     *
     * @param deviceId 设备表主键ID
     * @return 设备绑定记录，不存在时返回null
     */
    DeviceBindingEntity selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 在绑定记录当前状态符合预期时原子更新绑定状态。
     *
     * @param id 设备绑定记录主键ID
     * @param status 要更新成的绑定状态
     * @param expectedStatus 允许执行更新的当前绑定状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);
}
