package com.app;

import com.app.enums.PhotoFileStatusEnum;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据库照片状态契约测试。
 *
 * @author yanlei
 * @since 2026-09-10
 */
class PhotoFileStatusSchemaTest {
    /**
     * 验证数据库设计文件包含已删除状态。
     *
     * @throws Exception 读取数据库设计文件失败时抛出
     */
    @Test
    void shouldContainDeletedStatusInSchema() throws Exception {
        String schema = Files.readString(Path.of("..", "app-resource", "cloud-album-database-schema.sql"));
        assertTrue(schema.contains("'DELETED'"));
        assertTrue(PhotoFileStatusEnum.DELETED.getValue().equals("DELETED"));
    }
}
