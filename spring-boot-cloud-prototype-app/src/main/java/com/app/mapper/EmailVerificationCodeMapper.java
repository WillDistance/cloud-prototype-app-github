package com.app.mapper;

import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 验证码数据访问接口。 */
@Mapper
public interface EmailVerificationCodeMapper extends BaseMapper<EmailVerificationCodeEntity> {
    EmailVerificationCodeEntity selectByIdForUpdate(Long id);
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);
    EmailVerificationCodeEntity selectLatest(@Param("email") String email, @Param("purpose") String purpose);
    int markVerified(@Param("id") Long id);
    int incrementFailures(@Param("id") Long id);
}
