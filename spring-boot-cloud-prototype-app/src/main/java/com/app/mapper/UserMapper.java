package com.app.mapper;

import com.app.pojo.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_user表数据访问接口。
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    /**
     * 执行数据库查询操作。
     *
     * @param id 数据库查询参数
     * @return 数据库查询结果
     */
    UserEntity selectByIdForUpdate(Long id);

    /**
     * 执行数据库更新操作。
     *
     * @param id             数据库查询参数
     * @param status         数据库查询参数
     * @param expectedStatus 数据库查询参数
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    /**
     * 执行数据库查询操作。
     *
     * @param email 数据库查询参数
     * @return 数据库查询结果
     */
    UserEntity selectByEmail(@Param("email") String email);

    /**
     * 执行数据库更新操作。
     *
     * @param id           数据库查询参数
     * @param passwordHash 数据库查询参数
     * @return 受影响的记录数
     */
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    /**
     * 执行数据库更新操作。
     *
     * @param id 数据库查询参数
     * @return 受影响的记录数
     */
    int updateLastLogin(@Param("id") Long id);

    /**
     * 更新用户的通知语言偏好。
     *
     * @param id 用户ID
     * @param preferredLanguage 语言代码
     * @return 实际更新的记录数
     */
    int updatePreferredLanguage(@Param("id") Long id, @Param("preferredLanguage") String preferredLanguage);
}
