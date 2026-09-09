package com.app;

import com.app.enums.StoragePlanStatusEnum;
import com.app.service.impl.StoragePlanServiceImpl;
import com.app.service.impl.StorageServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 阶段10存储概览和套餐契约测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase10StorageTest {
    /**
     * 验证套餐状态枚举与数据库值一致。
     */
    @Test
    void shouldKeepStoragePlanStatusValues() {
        assertEquals("ACTIVE", StoragePlanStatusEnum.ACTIVE.getValue());
        assertEquals("OFF_SHELF", StoragePlanStatusEnum.OFF_SHELF.getValue());
        assertEquals("ARCHIVED", StoragePlanStatusEnum.ARCHIVED.getValue());
    }

    /**
     * 验证存储服务使用MyBatis-Plus基础实现。
     */
    @Test
    void shouldUseMybatisPlusServices() {
        assertEquals(com.baomidou.mybatisplus.spring.service.impl.ServiceImpl.class, StorageServiceImpl.class.getSuperclass());
        assertEquals(com.baomidou.mybatisplus.spring.service.impl.ServiceImpl.class, StoragePlanServiceImpl.class.getSuperclass());
    }
}
