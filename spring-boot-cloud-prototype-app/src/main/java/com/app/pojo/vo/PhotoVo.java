package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 相册查询响应模型
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class PhotoVo {
    private PhotoVo() {
    }

    /** 相册列表项。 */
    @Data
    @Accessors(chain = true)
    public static class ListItem {
        private String photoNo;
        private String fileName;
        private Integer widthPixels;
        private Integer heightPixels;
        private LocalDateTime takenTime;
        private LocalDateTime uploadedTime;
        private String thumbnailUrl;
    }

    /** 照片详情。 */
    @Data
    @Accessors(chain = true)
    public static class Detail {
        private String photoNo;
        private String fileName;
        private String originalMimeType;
        private Long originalSizeBytes;
        private Integer widthPixels;
        private Integer heightPixels;
        private LocalDateTime takenTime;
        private LocalDateTime uploadedTime;
        private String previewUrl;
    }

    /** 原图下载短签名地址。 */
    @Data
    @Accessors(chain = true)
    public static class DownloadUrl {
        private String downloadUrl;
    }
}
