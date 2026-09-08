package com.app.support.oss;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件元数据信息
 *
 * @author yanlei
 * @since 2026-08-15
 */
@Data
@Accessors(chain = true)
public class FileMetadata {
    /** 文件大小 */
    private long size;

    /** 响应的contentType */
    private String contentType;
}