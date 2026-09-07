package com.app.support.notification;

import com.app.enums.NotificationTypeEnum;
import com.app.enums.UserLanguageEnum;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * 从 classpath 读取并校验三语言通知模板。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Component
public class ClasspathNotificationTemplateProvider implements NotificationTemplateProvider {
    private final Map<UserLanguageEnum, Map<String, NotificationTemplate>> templates = new EnumMap<>(UserLanguageEnum.class);

    public ClasspathNotificationTemplateProvider() {
        for (UserLanguageEnum language : UserLanguageEnum.values()) {
            Map<String, NotificationTemplate> languageTemplates = new java.util.HashMap<>();
            for (NotificationTypeEnum type : NotificationTypeEnum.values()) {
                String path = "notification-templates/" + language.getValue() + "/" + type.getValue() + ".properties";
                Properties properties = new Properties();
                try (InputStream input = new ClassPathResource(path).getInputStream()) {
                    properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
                } catch (IOException exception) {
                    throw new UncheckedIOException("通知模板缺失: " + path, exception);
                }
                String subject = properties.getProperty("subject");
                String body = properties.getProperty("body");
                if (subject == null || subject.isBlank() || body == null || body.isBlank()) {
                    throw new IllegalStateException("通知模板字段不完整: " + path);
                }
                languageTemplates.put(type.getValue(), new NotificationTemplate(subject, body));
            }
            templates.put(language, Map.copyOf(languageTemplates));
        }
    }

    @Override
    public NotificationTemplate getTemplate(String templateKey, UserLanguageEnum language) {
        UserLanguageEnum selected = language == null ? UserLanguageEnum.EN : language;
        NotificationTemplate template = templates.getOrDefault(selected, templates.get(UserLanguageEnum.EN)).get(templateKey);
        if (template == null) {
            throw new IllegalArgumentException("通知模板不存在: " + templateKey);
        }
        return template;
    }
}
