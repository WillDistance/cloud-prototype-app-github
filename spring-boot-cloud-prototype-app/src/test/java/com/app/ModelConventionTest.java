package com.app;

import com.app.config.BaseFieldMetaObjectHandler;
import com.app.pojo.dto.TestDto;
import com.app.pojo.entity.BaseField;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 基础模型约定测试
 *
 * @author yanlei
 * @since 2026-09-05
 */
public class ModelConventionTest {

    public static void main(String[] args) throws Exception {
        verifySaveOrUpdateNameType();
        verifyDeleteIdValidationAnnotation();
        verifyBaseFieldAccessors();
        verifyBaseFieldTimeFillType();
    }

    private static void verifySaveOrUpdateNameType() throws Exception {
        Field nameField = TestDto.SaveOrUpdate.class.getDeclaredField("name");
        assertEquals(String.class, nameField.getType(), "SaveOrUpdate.name应使用String类型");
    }

    private static void verifyDeleteIdValidationAnnotation() throws Exception {
        Field idField = TestDto.Delete.class.getDeclaredField("id");
        if (!idField.isAnnotationPresent(NotNull.class)) {
            throw new AssertionError("Delete.id应使用jakarta.validation.constraints.NotNull注解");
        }
    }

    private static void verifyBaseFieldAccessors() throws Exception {
        assertEquals(String.class, BaseField.class.getMethod("getCreateBy").getReturnType(),
                "BaseField应提供createBy的Getter");
        assertEquals(BaseField.class, BaseField.class.getMethod("setCreateBy", String.class).getReturnType(),
                "BaseField应提供链式createBy Setter");
    }

    private static void verifyBaseFieldTimeFillType() {
        BaseField baseField = new BaseField();
        MetaObject metaObject = SystemMetaObject.forObject(baseField);
        BaseFieldMetaObjectHandler handler = new BaseFieldMetaObjectHandler(null);

        handler.insertFill(metaObject);

        if (baseField.getCreateTime() == null || baseField.getUpdateTime() == null) {
            throw new AssertionError("新增填充应设置LocalDateTime类型的创建和更新时间");
        }
        assertEquals(LocalDateTime.class, baseField.getCreateTime().getClass(),
                "createTime应填充LocalDateTime");
        assertEquals(LocalDateTime.class, baseField.getUpdateTime().getClass(),
                "updateTime应填充LocalDateTime");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "，期望：" + expected + "，实际：" + actual);
        }
    }
}
