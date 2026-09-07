package com.app.mapper;

import com.app.pojo.entity.DeviceBindingEntity;
import com.app.pojo.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * DeviceBindingEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface DeviceBindingMapper extends BaseMapper<DeviceBindingEntity> {
    /**
     * 按用户ID查询并锁定用户记录，供事务更新使用。
     *
     * @param userId 用户ID
     * @return 查询结果
     */
    UserEntity selectUserForUpdate(@Param("userId") Long userId);

    /**
     * 按用户ID查询对应记录。
     *
     * @param userId 用户ID
     * @return 查询结果
     */
    DeviceBindingEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 按设备ID查询设备绑定记录。
     *
     * @param deviceId 设备ID
     * @return 查询结果
     */
    DeviceBindingEntity selectByDeviceId(@Param("deviceId") Long deviceId);
}
