package com.app.support.photo;

import org.springframework.stereotype.Component;

/**
 * 图片处理占位实现，部署时应替换为真实解码、缩放与OSS上传实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class UnsupportedPhotoImageProcessor implements PhotoImageProcessor {
    @Override
    public ImageProcessingResult process(String originalKey, String thumbnailKey, String previewKey) {
        throw new IllegalStateException("尚未配置真实图片处理器");
    }
}
