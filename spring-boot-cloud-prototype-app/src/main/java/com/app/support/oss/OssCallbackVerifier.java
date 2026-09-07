package com.app.support.oss;

import com.app.pojo.dto.OssCallbackDto;

/**
 * OSS回调来源与签名校验抽象
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface OssCallbackVerifier {
    boolean verify(OssCallbackDto callback);
}
