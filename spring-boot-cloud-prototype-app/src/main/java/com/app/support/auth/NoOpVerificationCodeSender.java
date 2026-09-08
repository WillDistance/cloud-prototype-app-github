package com.app.support.auth;

import org.springframework.stereotype.Component;

/**
 * 不连接外部邮件服务的验证码发送适配器。
 */
@Component
public class NoOpVerificationCodeSender implements VerificationCodeSender {
    @Override
    public void send(String email, String code) {
        // 生产环境替换为邮件供应商；验证码不写入日志。
    }
}
