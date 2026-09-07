package com.app.support.objectstorage;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.internal.OSSHeaders;

import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 阿里云OSS PUT链接签发器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class OssUploadUrlSigner implements UploadUrlSigner {
    private final OSS oss;
    private final String bucketName;
    private final String callbackUrl;
    private final String callbackSignature;

    public OssUploadUrlSigner(OSS oss, String bucketName, String callbackUrl, String callbackSignature) {
        this.oss = oss;
        this.bucketName = bucketName;
        this.callbackUrl = callbackUrl;
        this.callbackSignature = callbackSignature;
    }

    public OssUploadUrlSigner(OSS oss, String bucketName) {
        this(oss, bucketName, "", "");
    }

    @Override
    public String createPutUrl(String objectKey, String contentType, LocalDateTime expireTime) {
        return createPutUrl(objectKey, contentType, expireTime, "");
    }

    @Override
    public String createPutUrl(String objectKey, String contentType, LocalDateTime expireTime,
                               String uploadNo) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey, HttpMethod.PUT);
        request.setExpiration(java.util.Date.from(expireTime.toInstant(ZoneOffset.UTC)));
        // 将Content-Type加入签名请求；客户端上传时必须使用相同请求头。
        request.setContentType(contentType);
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Content-Type", contentType);
        if (callbackUrl != null && !callbackUrl.isBlank()) {
            String callbackBody = "{\"eventId\":\"${etag}\",\"uploadNo\":\"${x:uploadNo}\","
                    + "\"objectKey\":\"${object}\",\"sizeBytes\":\"${size}\","
                    + "\"contentType\":\"${mimeType}\",\"callbackTime\":\"${x:callbackTime}\","
                    + "\"signature\":\"${x:signature}\"}";
            String callbackJson = "{\"callbackUrl\":\"" + escapeJson(callbackUrl)
                    + "\",\"callbackBody\":\"" + escapeJson(callbackBody) + "\"}";
            headers.put(OSSHeaders.OSS_HEADER_CALLBACK, Base64.getEncoder().encodeToString(
                    callbackJson.getBytes(StandardCharsets.UTF_8)));
            headers.put(OSSHeaders.OSS_HEADER_CALLBACK_VAR, Base64.getEncoder().encodeToString((
                    "{\\\"x:uploadNo\\\":\\\"" + escapeJson(uploadNo) + "\\\","
                            + "\\\"x:callbackTime\\\":\\\"" + escapeJson(expireTime.toString()) + "\\\","
                            + "\\\"x:signature\\\":\\\"" + escapeJson(callbackSignature) + "\\\"}")
                    .getBytes(StandardCharsets.UTF_8)));
        }
        request.setHeaders(headers);
        return oss.generatePresignedUrl(request).toString();
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
