package com.app.support.photo;

/**
 * 照片派生图处理抽象，生成最长边480px缩略图和1920px预览图
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface PhotoImageProcessor {
    ImageProcessingResult process(String originalKey, String thumbnailKey, String previewKey);
}
