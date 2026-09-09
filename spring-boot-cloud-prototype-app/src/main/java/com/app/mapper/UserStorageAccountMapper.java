package com.app.mapper;

import com.app.pojo.entity.UserStorageAccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户存储账户数据访问接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface UserStorageAccountMapper extends BaseMapper<UserStorageAccountEntity> {
    /**
     * 按主键查询并锁定用户存储账户。
     *
     * @param id 存储账户主键ID
     * @return 被锁定的存储账户，不存在时返回null
     */
    UserStorageAccountEntity selectByIdForUpdate(Long id);

    /**
     * 根据用户ID查询并锁定用户存储账户。
     *
     * @param userId 用户ID
     * @return 被锁定的存储账户，不存在时返回null
     */
    UserStorageAccountEntity selectByUserIdForUpdate(@Param("userId") Long userId);
}
