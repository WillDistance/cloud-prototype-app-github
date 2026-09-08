package com.app.service;

import com.app.pojo.dto.*;
import com.app.pojo.vo.AuthTokenVo;
import com.app.pojo.vo.UserVo;

/** 用户认证业务。 */
public interface AuthService {
    void sendRegisterCode(AuthEmailCodeRequest request, String ip);
    void sendResetPasswordCode(AuthEmailCodeRequest request, String ip);
    void verifyRegisterCode(VerifyCodeRequest request);
    void verifyResetPasswordCode(VerifyCodeRequest request);
    UserVo register(RegisterRequest request, String timeZone, String language);
    AuthTokenVo login(LoginRequest request);
    void resetPassword(ResetPasswordRequest request);
    void logout();
}
