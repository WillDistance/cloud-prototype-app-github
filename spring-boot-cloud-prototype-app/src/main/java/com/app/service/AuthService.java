package com.app.service;

import com.app.pojo.dto.AuthDto;
import com.app.pojo.vo.AuthLoginVo;
import com.app.pojo.vo.AuthRegisterVo;

/**
 * 认证服务接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface AuthService {
    /**
     * 生成并发送注册验证码。
     *
     * @param dto       注册验证码请求
     * @param requestIp 请求来源IP
     * @return 验证码是否发送成功
     */
    boolean sendRegisterCode(AuthDto.SendRegisterCode dto, String requestIp);

    /**
     * 校验注册验证码。
     *
     * @param dto 注册验证码校验请求
     * @return 验证码是否有效
     */
    boolean verifyRegisterCode(AuthDto.VerifyRegisterCode dto);

    /**
     * 校验注册信息并创建用户账号。
     *
     * @param dto            用户注册请求
     * @param timeZone       用户时区
     * @param acceptLanguage 请求语言
     * @return 注册用户信息
     */
    AuthRegisterVo register(AuthDto.Register dto, String timeZone, String acceptLanguage);

    /**
     * 校验用户登录信息并签发访问令牌。
     *
     * @param dto 用户登录请求
     * @return 登录结果，包含访问令牌
     */
    AuthLoginVo login(AuthDto.Login dto);

    /**
     * 生成并发送密码重置验证码。
     *
     * @param dto       密码重置验证码请求
     * @param requestIp 请求来源IP
     * @return 验证码是否发送成功
     */
    boolean sendResetPasswordCode(AuthDto.SendResetPasswordCode dto, String requestIp);

    /**
     * 校验密码重置验证码。
     *
     * @param dto 密码重置验证码校验请求
     * @return 验证码是否有效
     */
    boolean verifyResetPasswordCode(AuthDto.VerifyResetPasswordCode dto);

    /**
     * 校验验证码并更新用户密码。
     *
     * @param dto 密码重置请求
     * @return 密码是否更新成功
     */
    boolean resetPassword(AuthDto.ResetPassword dto);

    /**
     * 注销当前用户的访问令牌。
     *
     * @param tokenId 访问令牌ID
     * @return 注销是否成功
     */
    boolean logout(String tokenId);
}
