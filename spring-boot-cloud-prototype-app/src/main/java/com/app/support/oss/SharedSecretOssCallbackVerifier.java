package com.app.support.oss;

import com.app.pojo.dto.OssCallbackDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 固定密钥OSS回调校验器，生产环境应替换为云厂商公钥验签实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class SharedSecretOssCallbackVerifier implements OssCallbackVerifier {
    private final byte[] expected;

    public SharedSecretOssCallbackVerifier(@Value("${oss.callback-test-signature:local-callback-signature}") String expected) {
        this.expected = expected.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean verify(OssCallbackDto callback) {
        return callback != null && callback.getSignature() != null && MessageDigest.isEqual(expected,
                callback.getSignature().getBytes(StandardCharsets.UTF_8));
    }
}
