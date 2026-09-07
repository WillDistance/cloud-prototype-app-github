package com.app.mapper;

import com.app.pojo.entity.UserStorageAccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户存储账户数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface UserStorageAccountMapper extends BaseMapper<UserStorageAccountEntity> {
    /**
     * 按用户ID查询对应记录。
     *
     * @param userId 用户ID
     * @return 查询结果
     */
    UserStorageAccountEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 按用户ID查询并锁定存储账户记录。
     *
     * @param userId 用户ID
     * @return 用户存储账户记录
     */
    UserStorageAccountEntity selectByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * 增加用户存储账户的预留容量。
     *
     * @param userId 用户ID
     * @param bytes 容量字节数
     * @return 受影响的记录数
     */
    int increaseReservedBytes(@Param("userId") Long userId, @Param("bytes") Long bytes);

    /**
     * 减少用户存储账户的预留容量。
     *
     * @param userId 用户ID
     * @param bytes 容量字节数
     * @return 受影响的记录数
     */
    int decreaseReservedBytes(@Param("userId") Long userId, @Param("bytes") Long bytes);

    /**
     * 将用户存储账户的预留容量转为已使用容量。
     *
     * @param userId 用户ID
     * @param reservedBytes 预留容量字节数
     * @param usedBytes 已使用容量字节数
     * @return 受影响的记录数
     */
    int moveReservedToUsed(@Param("userId") Long userId, @Param("reservedBytes") Long reservedBytes,
                           @Param("usedBytes") Long usedBytes);

    /**
     * 减少用户存储账户的已使用容量。
     *
     * @param userId 用户ID
     * @param bytes 容量字节数
     * @return 受影响的记录数
     */
    int decreaseUsedBytes(@Param("userId") Long userId, @Param("bytes") Long bytes);
}
