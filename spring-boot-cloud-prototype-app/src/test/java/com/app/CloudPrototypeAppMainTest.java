package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用上下文启动测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
@ActiveProfiles("test")
@SpringBootTest
class CloudPrototypeAppMainTest {

    @Test
    void 应成功加载应用上下文() {
    }
}
