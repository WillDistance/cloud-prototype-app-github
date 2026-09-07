package com.app.support.verification;

import com.app.enums.UserLanguageEnum;

/**
 * 验证码发送接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface VerificationCodeSender {
    /**
     * 发送注册验证码
     *
     * @param email    邮箱
     * @param code     验证码
     * @param language 语言
     */
    void sendRegisterCode(String email, String code, UserLanguageEnum language);
}
