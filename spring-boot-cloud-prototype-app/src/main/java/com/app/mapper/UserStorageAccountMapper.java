package com.app.mapper;

import com.app.pojo.entity.UserStorageAccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 用户存储账户数据访问接口。 */
@Mapper
public interface UserStorageAccountMapper extends BaseMapper<UserStorageAccountEntity> {
    UserStorageAccountEntity selectByIdForUpdate(Long id);
}
