package com.app.mapper;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户存储账户 Mapper 测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class UserStorageAccountMapperTest {

    @Test
    void shouldDecreaseUsedBytesAtomicallyWithoutGoingNegative() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/mapper/UserStorageAccountMapper.xml")) {
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(sql.contains("used_bytes = used_bytes - #{bytes}"));
            assertTrue(sql.contains("used_bytes >= #{bytes}"));
            assertTrue(sql.contains("lock_version = lock_version + 1"));
        }
    }
}
