package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 照片原图下载地址响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class PhotoDownloadVo {
    /** 原图临时下载地址。 */
    private String downloadUrl;
}
