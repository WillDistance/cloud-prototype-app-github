package com.app.persistence;

import com.app.pojo.entity.BaseField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段一数据访问模型映射测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class DataAccessModelMappingTest {

    private static final Map<String, EntityExpectation> ENTITIES = new LinkedHashMap<>();

    static {
        expect("UserEntity", "t_user", "email", String.class);
        expect("EmailVerificationCodeEntity", "t_email_verification_code", "purpose", "com.app.enums.VerificationPurposeEnum");
        expect("DeviceEntity", "t_device", "deviceId", String.class);
        expect("DeviceBindingEntity", "t_device_binding", "bindUserTimeZone", String.class);
        expect("PlatformConfigEntity", "t_platform_config", "allowedExtensions", java.util.List.class);
        expect("StoragePlanEntity", "t_storage_plan", "priceCent", Long.class);
        expect("PaymentOrderEntity", "t_payment_order", "orderNo", String.class);
        expect("PaymentCallbackEntity", "t_payment_callback", "rawPayload", String.class);
        expect("UserStorageAccountEntity", "t_user_storage_account", "lockVersion", Long.class);
        expect("StorageEntitlementEntity", "t_storage_entitlement", "sourceType", "com.app.enums.EntitlementSourceTypeEnum");
        expect("UploadSessionEntity", "t_upload_session", "originalObjectKey", String.class);
        expect("PhotoEntity", "t_photo", "originalSha256", String.class);
        expect("PhotoFileEntity", "t_photo_file", "deleteReason", "com.app.enums.DeleteReasonEnum");
        expect("NotificationEntity", "t_notification", "notificationType", "com.app.enums.NotificationTypeEnum");
    }

    @Test
    void 十四个实体应准确映射表名主键和关键字段() throws Exception {
        assertEquals(14, ENTITIES.size());
        for (Map.Entry<String, EntityExpectation> entry : ENTITIES.entrySet()) {
            Class<?> entityClass = Class.forName("com.app.pojo.entity." + entry.getKey());
            EntityExpectation expected = entry.getValue();

            assertTrue(BaseField.class.isAssignableFrom(entityClass), entry.getKey() + "应继承BaseField");
            TableName tableName = entityClass.getAnnotation(TableName.class);
            assertEquals(expected.tableName(), tableName.value());
            Field id = entityClass.getDeclaredField("id");
            TableId tableId = id.getAnnotation(TableId.class);
            assertEquals("id", tableId.value());
            assertEquals(IdType.AUTO, tableId.type());
            Field keyField = entityClass.getDeclaredField(expected.keyField());
            assertEquals(expected.keyType(), keyField.getType());
        }
    }

    @Test
    void 平台配置JSON字段应启用Jackson类型处理器() throws Exception {
        Class<?> entityClass = Class.forName("com.app.pojo.entity.PlatformConfigEntity");
        assertTrue(entityClass.getAnnotation(TableName.class).autoResultMap());
        assertJsonField(entityClass, "allowedExtensions");
        assertJsonField(entityClass, "allowedMimeTypes");
    }

    @Test
    void 十四个Mapper应绑定对应实体泛型() throws Exception {
        for (String entitySimpleName : ENTITIES.keySet()) {
            String mapperName = entitySimpleName.replace("Entity", "Mapper");
            Class<?> mapperClass = Class.forName("com.app.mapper." + mapperName);
            ParameterizedType baseMapperType = (ParameterizedType) mapperClass.getGenericInterfaces()[0];
            assertEquals(BaseMapper.class, baseMapperType.getRawType());
            assertEquals("com.app.pojo.entity." + entitySimpleName,
                    ((Class<?>) baseMapperType.getActualTypeArguments()[0]).getName());
        }
    }

    private static void assertJsonField(Class<?> entityClass, String fieldName) throws Exception {
        TableField tableField = entityClass.getDeclaredField(fieldName).getAnnotation(TableField.class);
        assertEquals(JacksonTypeHandler.class, tableField.typeHandler());
    }

    private static void expect(String entity, String table, String field, Class<?> type) {
        ENTITIES.put(entity, new EntityExpectation(table, field, type));
    }

    private static void expect(String entity, String table, String field, String typeName) {
        try {
            expect(entity, table, field, Class.forName(typeName));
        } catch (ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private record EntityExpectation(String tableName, String keyField, Class<?> keyType) {
    }
}
