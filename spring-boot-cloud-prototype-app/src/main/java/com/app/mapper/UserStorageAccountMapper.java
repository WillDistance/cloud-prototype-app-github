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

    /**
     * 在预留容量足够时原子增加用户存储预留容量。
     *
     * @param userId 用户ID
     * @param bytes 待增加的预留容量，单位为字节
     * @return 受影响的账户记录数
     */
    int increaseReservedBytes(@Param("userId") Long userId, @Param("bytes") Long bytes);

    /**
     * 原子减少用户存储预留容量。
     *
     * @param userId 用户ID
     * @param bytes 待释放的预留容量，单位为字节
     * @return 受影响的账户记录数
     */
    int decreaseReservedBytes(@Param("userId") Long userId, @Param("bytes") Long bytes);

    /**
     * 增加用户已确认使用容量。
     *
     * @param userId 用户ID
     * @param bytes 已确认增加的容量，单位为字节
     * @return 受影响的账户记录数
     */
    int increaseUsedBytes(@Param("userId") Long userId, @Param("bytes") Long bytes);
}
