package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.*;
import com.app.pojo.vo.AuthTokenVo;
import com.app.pojo.vo.UserVo;
import com.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * 发送注册验证码。
     *
     * @param request     注册验证码请求
     * @param httpRequest 当前HTTP请求，用于获取请求来源IP
     * @return 空数据成功响应
     */
    @PostMapping("/sendRegisterCode")
    public CommonResult<Void> sendRegisterCode(@Valid @RequestBody AuthEmailCodeRequest request, HttpServletRequest httpRequest) {
        authService.sendRegisterCode(request, httpRequest.getRemoteAddr());
        return CommonResult.success(null);
    }

    /**
     * 校验注册验证码。
     *
     * @param request 验证码校验请求
     * @return 空数据成功响应
     */
    @PostMapping("/verifyRegisterCode")
    public CommonResult<Void> verifyRegisterCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyRegisterCode(request);
        return CommonResult.success(null);
    }

    /**
     * 校验注册信息并创建用户账号。
     *
     * @param request  用户注册请求
     * @param timeZone 用户当前IANA时区
     * @param language 用户当前语言
     * @return 注册用户资料
     */
    @PostMapping("/register")
    public CommonResult<UserVo> register(@Valid @RequestBody RegisterRequest request,
                                         @RequestHeader(value = "X-Time-Zone", required = false) String timeZone,
                                         @RequestHeader(value = "Accept-Language", required = false) String language) {
        return CommonResult.success(authService.register(request, timeZone, language));
    }

    /**
     * 校验登录信息并签发JWT访问令牌。
     *
     * @param request 用户登录请求
     * @return 登录令牌信息
     */
    @PostMapping("/login")
    public CommonResult<AuthTokenVo> login(@Valid @RequestBody LoginRequest request) {
        return CommonResult.success(authService.login(request));
    }

    /**
     * 使用Refresh Token换取新的双令牌。
     *
     * @param request Refresh Token刷新请求
     * @return 新的访问令牌和刷新令牌
     */
    @PostMapping("/refresh")
    public CommonResult<AuthTokenVo> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return CommonResult.success(authService.refresh(request));
    }

    /**
     * 发送密码重置验证码。
     *
     * @param request     密码重置验证码请求
     * @param httpRequest 当前HTTP请求，用于获取请求来源IP
     * @return 空数据成功响应
     */
    @PostMapping("/sendResetPasswordCode")
    public CommonResult<Void> sendResetPasswordCode(@Valid @RequestBody AuthEmailCodeRequest request, HttpServletRequest httpRequest) {
        authService.sendResetPasswordCode(request, httpRequest.getRemoteAddr());
        return CommonResult.success(null);
    }

    /**
     * 校验密码重置验证码。
     *
     * @param request 验证码校验请求
     * @return 空数据成功响应
     */
    @PostMapping("/verifyResetPasswordCode")
    public CommonResult<Void> verifyResetPasswordCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyResetPasswordCode(request);
        return CommonResult.success(null);
    }

    /**
     * 使用已验证的验证码重置密码。
     *
     * @param request 密码重置请求
     * @return 空数据成功响应
     */
    @PostMapping("/resetPassword")
    public CommonResult<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return CommonResult.success(null);
    }

    /**
     * 撤销当前登录会话的Refresh Token并清理登录上下文。
     *
     * @param request 当前登录会话的Refresh Token请求
     * @return 空数据成功响应
     */
    @PostMapping("/logout")
    public CommonResult<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(request);
        return CommonResult.success(null);
    }
}
