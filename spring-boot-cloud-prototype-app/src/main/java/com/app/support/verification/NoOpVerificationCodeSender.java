package com.app.support.verification;

import com.app.enums.UserLanguageEnum;
import org.springframework.stereotype.Component;

/**
 * 不实际发送邮件的验证码发送实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class NoOpVerificationCodeSender implements VerificationCodeSender {
    @Override
    public void sendRegisterCode(String email, String code, UserLanguageEnum language) {
        // 原型阶段不连接邮件服务；生产环境替换此实现。
    }
}
