package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.AuthDto;
import com.app.pojo.vo.AuthLoginVo;
import com.app.pojo.vo.AuthRegisterVo;
import com.app.security.UserContextHolder;
import com.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * 发送注册验证码。
     */
    @PostMapping("/sendRegisterCode")
    public CommonResult<Boolean> sendRegisterCode(@Valid @RequestBody AuthDto.SendRegisterCode dto,
                                                  HttpServletRequest request) {
        return CommonResult.success(authService.sendRegisterCode(dto, request.getRemoteAddr()));
    }

    /**
     * 验证注册验证码。
     */
    @PostMapping("/verifyRegisterCode")
    public CommonResult<Boolean> verifyRegisterCode(@Valid @RequestBody AuthDto.VerifyRegisterCode dto) {
        return CommonResult.success(authService.verifyRegisterCode(dto));
    }

    /**
     * 注册账号。
     */
    @PostMapping("/register")
    public CommonResult<AuthRegisterVo> register(@Valid @RequestBody AuthDto.Register dto,
                                                 @RequestHeader(value = "X-Time-Zone", required = false) String timeZone,
                                                 @RequestHeader(value = "Accept-Language", required = false) String language) {
        return CommonResult.success(authService.register(dto, timeZone, language));
    }

    @PostMapping("/login")
    public CommonResult<AuthLoginVo> login(@Valid @RequestBody AuthDto.Login dto) {
        return CommonResult.success(authService.login(dto));
    }

    @PostMapping("/sendResetPasswordCode")
    public CommonResult<Boolean> sendResetPasswordCode(@Valid @RequestBody AuthDto.SendResetPasswordCode dto, HttpServletRequest request) {
        return CommonResult.success(authService.sendResetPasswordCode(dto, request.getRemoteAddr()));
    }

    @PostMapping("/verifyResetPasswordCode")
    public CommonResult<Boolean> verifyResetPasswordCode(@Valid @RequestBody AuthDto.VerifyResetPasswordCode dto) {
        return CommonResult.success(authService.verifyResetPasswordCode(dto));
    }

    @PostMapping("/resetPassword")
    public CommonResult<Boolean> resetPassword(@Valid @RequestBody AuthDto.ResetPassword dto) {
        return CommonResult.success(authService.resetPassword(dto));
    }

    @PostMapping("/logout")
    public CommonResult<Boolean> logout() {
        return CommonResult.success(authService.logout(UserContextHolder.get().tokenId()));
    }
}
