package com.app.config;

import com.app.pojo.entity.BaseField;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 基础字段自动填充处理器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class BaseFieldMetaObjectHandlerTest {

    @Test
    void 应使用UTC时间填充审计字段() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-06T00:00:00Z"), ZoneOffset.UTC);
        BaseFieldMetaObjectHandler handler = new BaseFieldMetaObjectHandler(clock, null);
        BaseField baseField = new BaseField();
        MetaObject metaObject = SystemMetaObject.forObject(baseField);

        handler.insertFill(metaObject);

        assertEquals(LocalDateTime.of(2026, 9, 6, 0, 0), baseField.getCreateTime());
        assertEquals(LocalDateTime.of(2026, 9, 6, 0, 0), baseField.getUpdateTime());
    }
}
