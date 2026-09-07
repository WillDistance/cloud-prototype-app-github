package com.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本地基础设施连通性测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
@ActiveProfiles("test")
@SpringBootTest
class InfrastructureConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void 应成功连接MySQL() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1));
        }
    }

    @Test
    void 应完成Redis写入读取过期时间和删除() {
        String key = "test:infrastructure:" + UUID.randomUUID();
        try {
            stringRedisTemplate.opsForValue().set(key, "ok", Duration.ofSeconds(30));
            assertEquals("ok", stringRedisTemplate.opsForValue().get(key));
            Long expire = stringRedisTemplate.getExpire(key);
            assertTrue(expire != null && expire > 0);
        } finally {
            stringRedisTemplate.delete(key);
        }
    }
}
