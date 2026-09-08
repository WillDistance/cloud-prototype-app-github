package com.app.mapper;

import com.app.pojo.entity.UserStorageAccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户存储账户数据访问接口。
 */
@Mapper
public interface UserStorageAccountMapper extends BaseMapper<UserStorageAccountEntity> {
    /**
     * 执行数据库查询操作。
     *
     * @param id 数据库查询参数
     * @return 数据库查询结果
     */
    UserStorageAccountEntity selectByIdForUpdate(Long id);
}
