package com.app.mapper;

import com.app.pojo.entity.PlatformConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_platform_config表数据访问接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface PlatformConfigMapper extends BaseMapper<PlatformConfigEntity> {
    /**
     * 按主键查询并锁定平台配置记录。
     *
     * @param id 平台配置记录主键ID
     * @return 被锁定的平台配置，不存在时返回null
     */
    PlatformConfigEntity selectByIdForUpdate(Long id);

    /**
     * 在配置当前状态符合预期时原子更新配置状态。
     *
     * @param id 平台配置记录主键ID
     * @param status 要更新成的配置状态
     * @param expectedStatus 允许执行更新的当前配置状态
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    /**
     * 查询当前生效的平台配置。
     *
     * @return 当前生效的平台配置，不存在时返回null
     */
    PlatformConfigEntity selectActive();
}
