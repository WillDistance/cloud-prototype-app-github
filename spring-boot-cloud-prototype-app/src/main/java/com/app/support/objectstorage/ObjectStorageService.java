package com.app.support.objectstorage;

/**
 * 对象存储操作抽象
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface ObjectStorageService {
    ObjectMetadata stat(String objectKey);

    void delete(String objectKey);
}
