package com.app.support.auth;

/** 验证码发送抽象，正式邮件供应商可替换实现。 */
public interface VerificationCodeSender {
    void send(String email, String code);
}
