package com.app;

import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段一数据库实体、枚举和Mapper契约测试
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase1DatabaseContractTest {

    private static final Path DDL = Path.of("../app-resource/cloud-album-database-schema.sql");
    private static final Path ENTITY_DIR = Path.of("src/main/java/com/app/pojo/entity");
    private static final Path XML_DIR = Path.of("src/main/resources/mapper");

    private static final Map<String, String> TABLES = Map.ofEntries(
            Map.entry("t_user", "UserEntity"), Map.entry("t_email_verification_code", "EmailVerificationCodeEntity"),
            Map.entry("t_device", "DeviceEntity"), Map.entry("t_device_binding", "DeviceBindingEntity"),
            Map.entry("t_platform_config", "PlatformConfigEntity"), Map.entry("t_storage_plan", "StoragePlanEntity"),
            Map.entry("t_payment_order", "PaymentOrderEntity"), Map.entry("t_payment_callback", "PaymentCallbackEntity"),
            Map.entry("t_user_storage_account", "UserStorageAccountEntity"), Map.entry("t_storage_entitlement", "StorageEntitlementEntity"),
            Map.entry("t_photo_file", "PhotoFileEntity"), Map.entry("t_notification", "NotificationEntity"));

    @Test
    void ddlHasTwelveBusinessTablesAndEachEntityMapsToOneTable() throws Exception {
        String ddl = Files.readString(DDL);
        assertEquals(12, Pattern.compile("CREATE TABLE IF NOT EXISTS `t_").matcher(ddl).results().count());
        for (Map.Entry<String, String> entry : TABLES.entrySet()) {
            Class<?> entity = Class.forName("com.app.pojo.entity." + entry.getValue());
            assertEquals(entry.getKey(), entity.getAnnotation(TableName.class).value());
            assertTrue(Files.exists(ENTITY_DIR.resolve(entry.getValue() + ".java")));
        }
    }

    @Test
    void everyMapperXmlHasNamespaceResultMapColumnListAndRequiredStatements() throws Exception {
        for (Map.Entry<String, String> entry : TABLES.entrySet()) {
            String simple = entry.getValue().replace("Entity", "Mapper");
            Path file = XML_DIR.resolve(simple + ".xml");
            String xml = Files.readString(file);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            assertEquals("com.app.mapper." + simple, document.getDocumentElement().getAttribute("namespace"));
            assertTrue(xml.contains("id=\"BaseResultMap\""));
            assertTrue(xml.contains("id=\"Base_Column_List\""));
            assertTrue(xml.contains("id=\"selectByIdForUpdate\""));
            if (xml.contains("id=\"updateStatusById\"")) {
                assertTrue(xml.contains("status = #{status}"));
                assertTrue(xml.contains("status = #{expectedStatus}"));
            }
        }
    }

    @Test
    void enumContractsContainOnlyDdlValues() {
        assertEquals(List.of("ACTIVE", "LOCKED", "DISABLED"), enumNames(com.app.enums.UserStatusEnum.class));
        assertEquals(List.of("REGISTER", "RESET_PASSWORD"), enumNames(com.app.enums.VerificationPurposeEnum.class));
        assertEquals(List.of("UNBOUND", "BOUND", "DISABLED"), enumNames(com.app.enums.DeviceStatusEnum.class));
        assertEquals(List.of("ORIGINAL", "THUMBNAIL", "PREVIEW"), enumNames(com.app.enums.PhotoFileTypeEnum.class));
    }

    private List<String> enumNames(Class<? extends Enum<?>> type) {
        return List.of(type.getEnumConstants()).stream().map(Enum::name).toList();
    }
}
