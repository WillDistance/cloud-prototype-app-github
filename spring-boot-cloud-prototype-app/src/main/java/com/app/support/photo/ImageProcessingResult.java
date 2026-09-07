package com.app.support.photo;

import com.app.support.objectstorage.ObjectMetadata;
/**
 * 原图与派生图处理结果
 *
 * @author yanlei
 * @since 2026-09-06
 */
public record ImageProcessingResult(ObjectMetadata original, ObjectMetadata thumbnail, ObjectMetadata preview) {
}
