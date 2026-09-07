package com.app.mapper;

import com.app.pojo.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
