package com.app.support.notification;

import com.app.enums.UserLanguageEnum;

/**
 * 通知模板资源访问接口
 *
 * <p>模板按 {@code classpath:/notification-templates/{language}/{templateKey}.properties}
 * 组织，语言目录固定为 {@code zh-CN}、{@code en}、{@code de}。模板属性至少包含
 * {@code subject} 和 {@code body}，变量使用 {@code {variableName}} 占位。</p>
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface NotificationTemplateProvider {
    /**
     * 读取指定语言的通知模板。
     *
     * @param templateKey 模板键
     * @param language    目标语言
     * @return 通知模板
     */
    NotificationTemplate getTemplate(String templateKey, UserLanguageEnum language);

    /**
     * 通知模板内容。
     *
     * @param subject 通知主题
     * @param body    通知正文
     */
    record NotificationTemplate(String subject, String body) {
    }
}
