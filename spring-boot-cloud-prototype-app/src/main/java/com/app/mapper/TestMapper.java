package com.app.mapper;

import com.app.pojo.entity.TestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试数据访问接口
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Mapper
public interface TestMapper extends BaseMapper<TestEntity> {
}
