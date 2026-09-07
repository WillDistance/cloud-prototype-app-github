package com.app.support.verification;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

/**
 * 注册验证码Redis状态存储
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface RegisterCodeCache {
    Duration CODE_TTL = Duration.ofMinutes(5);
    Duration SEND_INTERVAL = Duration.ofSeconds(60);
    int MAX_ATTEMPTS = 5;

    enum VerifyResult {VERIFIED, INVALID, EXPIRED, ATTEMPTS_EXCEEDED}

    boolean requestCode(String email, CodeState state);

    VerifyResult verifyCode(String email, String code, PasswordEncoder passwordEncoder);

    boolean consumeVerified(String email);

    record CodeState(Long recordId, String codeHash) {
    }
}
