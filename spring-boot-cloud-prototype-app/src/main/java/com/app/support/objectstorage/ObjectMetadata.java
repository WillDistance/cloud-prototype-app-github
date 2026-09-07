package com.app.support.objectstorage;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OSS对象元数据
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class ObjectMetadata {
    private String objectKey;
    private Long sizeBytes;
    private String contentType;
    private String sha256;
    private Integer widthPixels;
    private Integer heightPixels;
}
