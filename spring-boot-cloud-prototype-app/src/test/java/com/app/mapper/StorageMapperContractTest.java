package com.app.mapper;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 存储查询Mapper契约测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class StorageMapperContractTest {
    @Test
    void entitlementQueryShouldUseExactEffectiveAndExpiryBoundary() throws Exception {
        String sql = xml("/mapper/StorageEntitlementMapper.xml");
        assertTrue(sql.contains("effective_time &lt;= #{now}"));
        assertTrue(sql.contains("expire_time > #{now}"));
        assertTrue(sql.contains("status = 'ACTIVE'"));
        assertTrue(sql.contains("ORDER BY expire_time ASC"));
    }

    @Test
    void planQueryShouldReadOnlyActiveAndEffectivePlans() throws Exception {
        String sql = xml("/mapper/StoragePlanMapper.xml");
        assertTrue(sql.contains("status = 'ACTIVE'"));
        assertTrue(sql.contains("effective_time &lt;= #{now}"));
        assertTrue(sql.contains("ORDER BY sort_order ASC"));
    }

    private String xml(String resource) throws Exception {
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
