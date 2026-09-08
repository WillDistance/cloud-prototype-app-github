package com.app.mapper;

import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 验证码数据访问接口。
 */
@Mapper
public interface EmailVerificationCodeMapper extends BaseMapper<EmailVerificationCodeEntity> {
    /**
     * 执行数据库查询操作。
     *
     * @param id 数据库查询参数
     * @return 数据库查询结果
     */
    EmailVerificationCodeEntity selectByIdForUpdate(Long id);

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
     * @param email   数据库查询参数
     * @param purpose 数据库查询参数
     * @return 数据库查询结果
     */
    EmailVerificationCodeEntity selectLatest(@Param("email") String email, @Param("purpose") String purpose);

    /**
     * 执行数据库更新操作。
     *
     * @param id 数据库查询参数
     * @return 受影响的记录数
     */
    int markVerified(@Param("id") Long id);

    /**
     * 执行数据库更新操作。
     *
     * @param id 数据库查询参数
     * @return 受影响的记录数
     */
    int incrementFailures(@Param("id") Long id);
}
