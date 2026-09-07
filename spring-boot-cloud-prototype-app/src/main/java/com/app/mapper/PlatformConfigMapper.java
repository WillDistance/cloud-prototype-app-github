package com.app.mapper;

import com.app.pojo.entity.PlatformConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * PlatformConfigEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface PlatformConfigMapper extends BaseMapper<PlatformConfigEntity> {
    /**
     * 查询指定时刻生效的平台配置。
     *
     * @param now 当前UTC时间
     * @return 查询结果
     */
    PlatformConfigEntity selectCurrentActive(@Param("now") LocalDateTime now);
}
