package com.app.mapper;

import com.app.pojo.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** t_user表数据访问接口。 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    UserEntity selectByIdForUpdate(Long id);
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);
    UserEntity selectByEmail(@Param("email") String email);
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
    int updateLastLogin(@Param("id") Long id);
}
