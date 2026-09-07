package com.app.mapper;

import com.app.enums.VerificationStatusEnum;
import com.app.pojo.entity.EmailVerificationCodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * EmailVerificationCodeEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface EmailVerificationCodeMapper extends BaseMapper<EmailVerificationCodeEntity> {
    /**
     * 验证码处于指定状态时，原子更新验证码状态。
     *
     * @param id 记录ID
     * @param current 期望的当前验证码状态
     * @param target 目标验证码状态
     * @return 受影响的记录数
     */
    int updateStatusIfCurrent(@Param("id") Long id,
                              @Param("current") VerificationStatusEnum current,
                              @Param("target") VerificationStatusEnum target);
}
