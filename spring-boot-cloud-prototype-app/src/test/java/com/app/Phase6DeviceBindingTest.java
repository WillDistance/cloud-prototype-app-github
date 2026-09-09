package com.app;

import com.app.enums.DeviceStatusEnum;
import com.app.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 阶段6设备绑定契约测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase6DeviceBindingTest {
    /**
     * 验证设备状态枚举的持久化值符合数据库约定。
     */
    @Test
    void shouldKeepDeviceStatusValues() {
        assertEquals("UNBOUND", DeviceStatusEnum.UNBOUND.getValue());
        assertEquals("BOUND", DeviceStatusEnum.BOUND.getValue());
        assertEquals("DISABLED", DeviceStatusEnum.DISABLED.getValue());
    }

    /**
     * 验证设备服务继承MyBatis-Plus服务实现基类。
     */
    @Test
    void shouldUseMybatisPlusServiceImplementation() {
        assertEquals(com.baomidou.mybatisplus.spring.service.impl.ServiceImpl.class,
                DeviceServiceImpl.class.getSuperclass());
    }
}
