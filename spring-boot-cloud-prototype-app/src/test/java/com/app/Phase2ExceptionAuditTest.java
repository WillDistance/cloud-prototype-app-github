package com.app;

import com.app.config.BaseFieldMetaObjectHandler;
import com.app.pojo.entity.BaseField;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 阶段二异常体系和审计字段测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase2ExceptionAuditTest {
    @Test
    void 未登录时审计字段填充不应抛出认证异常() {
        BaseField field = new BaseField();
        assertDoesNotThrow(() -> new BaseFieldMetaObjectHandler().insertFill(SystemMetaObject.forObject(field)));
        assertNotNull(field.getCreateTime());
        assertNotNull(field.getUpdateTime());
    }
}
