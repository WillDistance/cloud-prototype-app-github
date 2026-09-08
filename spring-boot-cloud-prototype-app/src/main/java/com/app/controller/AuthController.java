package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.*;
import com.app.pojo.vo.*;
import com.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 认证控制器。 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthService authService;
    @PostMapping("/sendRegisterCode") public CommonResult<Void> sendRegisterCode(@Valid @RequestBody AuthEmailCodeRequest r, HttpServletRequest h) { authService.sendRegisterCode(r, h.getRemoteAddr()); return CommonResult.success(null); }
    @PostMapping("/verifyRegisterCode") public CommonResult<Void> verifyRegisterCode(@Valid @RequestBody VerifyCodeRequest r) { authService.verifyRegisterCode(r); return CommonResult.success(null); }
    @PostMapping("/register") public CommonResult<UserVo> register(@Valid @RequestBody RegisterRequest r, @RequestHeader(value="X-Time-Zone", required=false) String tz, @RequestHeader(value="Accept-Language", required=false) String lang) { return CommonResult.success(authService.register(r, tz, lang)); }
    @PostMapping("/login") public CommonResult<AuthTokenVo> login(@Valid @RequestBody LoginRequest r) { return CommonResult.success(authService.login(r)); }
    @PostMapping("/sendResetPasswordCode") public CommonResult<Void> sendResetPasswordCode(@Valid @RequestBody AuthEmailCodeRequest r, HttpServletRequest h) { authService.sendResetPasswordCode(r, h.getRemoteAddr()); return CommonResult.success(null); }
    @PostMapping("/verifyResetPasswordCode") public CommonResult<Void> verifyResetPasswordCode(@Valid @RequestBody VerifyCodeRequest r) { authService.verifyResetPasswordCode(r); return CommonResult.success(null); }
    @PostMapping("/resetPassword") public CommonResult<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest r) { authService.resetPassword(r); return CommonResult.success(null); }
    @PostMapping("/logout") public CommonResult<Void> logout() { authService.logout(); return CommonResult.success(null); }
}
