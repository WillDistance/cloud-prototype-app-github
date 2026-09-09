package com.app.service;

import com.app.pojo.dto.*;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.AuthTokenVo;
import com.app.pojo.vo.UserVo;
import com.app.pojo.vo.UserProfileVo;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 用户认证业务。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface AuthService extends IService<UserEntity> {
    /**
     * 生成并发送注册验证码。
     *
     * @param request 注册验证码请求
     * @param ip      请求来源IP
     */
    void sendRegisterCode(AuthEmailCodeRequest request, String ip);

    /**
     * 生成并发送密码重置验证码。
     *
     * @param request 密码重置验证码请求
     * @param ip      请求来源IP
     */
    void sendResetPasswordCode(AuthEmailCodeRequest request, String ip);

    /**
     * 校验注册验证码。
     *
     * @param request 验证码校验请求
     */
    void verifyRegisterCode(VerifyCodeRequest request);

    /**
     * 校验密码重置验证码。
     *
     * @param request 验证码校验请求
     */
    void verifyResetPasswordCode(VerifyCodeRequest request);

    /**
     * 校验注册信息并创建用户账号。
     *
     * @param request  用户注册请求
     * @param timeZone 用户当前IANA时区
     * @param language 用户当前语言
     * @return 注册用户资料
     */
    UserVo register(RegisterRequest request, String timeZone, String language);

    /**
     * 查询当前登录用户资料。
     *
     * @return 当前用户的邮箱、时区和语言偏好
     */
    UserProfileVo getProfile();

    /**
     * 更新当前登录用户的通知语言偏好。
     *
     * @param request 语言偏好更新请求
     */
    void updateLanguage(LanguageUpdateRequest request);

    /**
     * 校验用户登录信息并签发JWT访问令牌。
     *
     * @param request 用户登录请求
     * @return 登录令牌信息
     */
    AuthTokenVo login(LoginRequest request);

    /**
     * 使用Refresh Token轮换登录令牌。
     *
     * @param request Refresh Token刷新请求
     * @return 新的双令牌信息
     */
    AuthTokenVo refresh(RefreshTokenRequest request);

    /**
     * 使用已验证的验证码重置密码。
     *
     * @param request 密码重置请求
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * 撤销当前登录会话的Refresh Token并清理登录上下文。
     *
     * @param request 当前登录会话的Refresh Token请求
     */
    void logout(RefreshTokenRequest request);
}
