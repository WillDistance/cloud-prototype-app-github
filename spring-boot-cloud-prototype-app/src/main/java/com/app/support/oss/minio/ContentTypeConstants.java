
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023‑2024. All rights reserved.
 */
package com.app.support.oss.minio;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型和ContentType对应关系
 *
 * @author yanlei
 * @since 2022‑10‑08
 */
public class ContentTypeConstants {
    /**
     * 文件类型和ContentType对应关系存储map
     */
    public static final Map<String, String> CONTENT_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("gif", "image/gif");
        put("java", "text/x‑java");
        put("jpeg", "image/jpeg");
        put("jpg", "image/jpeg");
        put("json", "application/json");
        put("md", "application/x‑genesis‑rom");
        put("mp3", "audio/mpeg");
        put("mp4", "video/mp4");
        put("p12", "application/x‑pkcs12");
        put("pdf", "application/pdf");
        put("png", "image/png");
        put("ppt", "application/vnd.ms‑powerpoint");
        put("pptx", "application/vnd.openxmlformats‑officedocument.presentationml.presentation");
        put("py", "text/x‑python");
        put("rar", "application/x‑rar");
        put("raw", "image/x‑panasonic‑raw");
        put("svg", "image/svg+xml");
        put("tar.gz", "application/x‑compressed‑tar");
        put("xlm", "application/vnd.ms‑excel");
        put("xls", "application/vnd.ms‑excel");
        put("xlsm", "application/vnd.openxmlformats‑officedocument.spreadsheetml.sheet");
        put("xlsx", "application/vnd.openxmlformats‑officedocument.spreadsheetml.sheet");
        put("xml", "application/xml");
        put("zip", "application/zip");
    }});
}
