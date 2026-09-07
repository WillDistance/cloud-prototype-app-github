package com.app.service.impl;

import com.app.enums.NotificationStatusEnum;
import com.app.enums.NotificationTypeEnum;
import com.app.enums.UserLanguageEnum;
import com.app.mapper.NotificationMapper;
import com.app.mapper.UserMapper;
import com.app.pojo.entity.NotificationEntity;
import com.app.pojo.entity.UserEntity;
import com.app.service.NotificationService;
import com.app.support.notification.NotificationMessage;
import com.app.support.notification.NotificationSender;
import com.app.support.notification.NotificationTemplateProvider;
import com.app.utils.UserTimeZoneUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 多语言通知创建、发送和重试服务。
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    private static final int MAX_RETRIES = 3;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final NotificationTemplateProvider templates;
    private final NotificationSender sender;

    public NotificationServiceImpl(NotificationMapper notificationMapper, UserMapper userMapper,
                                   NotificationTemplateProvider templates, NotificationSender sender) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.templates = templates;
        this.sender = sender;
    }

    @Override
    @Transactional
    public NotificationEntity create(Long userId, String recipient, NotificationTypeEnum type,
                                     com.app.enums.NotificationChannelEnum channel, String idempotencyKey,
                                     Map<String, Object> variables) {
        NotificationEntity existing = notificationMapper.selectOne(new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getIdempotencyKey, idempotencyKey).last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        UserEntity user = userMapper.selectById(userId);
        UserLanguageEnum language = user == null || user.getPreferredLanguage() == null
                ? UserLanguageEnum.EN : user.getPreferredLanguage();
        String zone = user == null || user.getTimeZone() == null ? "UTC" : user.getTimeZone();
        UserTimeZoneUtil.requireIanaZone(zone);
        Map<String, Object> safeVariables = variables == null ? Map.of() : Map.copyOf(variables);
        NotificationTemplateProvider.NotificationTemplate template = templates.getTemplate(type.getValue(), language);
        String subject = render(template.subject(), redactSensitive(safeVariables), language, zone);
        String content = render(template.body(), redactSensitive(safeVariables), language, zone);
        NotificationEntity entity = new NotificationEntity().setNotificationNo("NT-" + UUID.randomUUID())
                .setUserId(userId).setNotificationType(type).setLanguageCode(language).setChannel(channel)
                .setRecipient(recipient).setSubject(subject).setContent(content).setIdempotencyKey(idempotencyKey)
                .setStatus(NotificationStatusEnum.PENDING).setRetryCount(0);
        notificationMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public void send(Long notificationId, Clock clock) {
        send(notificationId, LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public void send(Long notificationId, LocalDateTime now) {
        NotificationEntity notification = notificationMapper.selectById(notificationId);
        if (notification == null || notification.getStatus() == NotificationStatusEnum.SENT
                || notification.getStatus() == NotificationStatusEnum.FAILED) return;
        int retries = notification.getRetryCount() == null ? 0 : notification.getRetryCount();
        try {
            notificationMapper.updateById(notification.setStatus(NotificationStatusEnum.SENDING));
            sender.send(new NotificationMessage(notification.getChannel(), notification.getRecipient(),
                    notification.getSubject(), notification.getContent(), Map.of()));
            notificationMapper.updateById(notification.setStatus(NotificationStatusEnum.SENT).setSentTime(now)
                    .setNextRetryTime(null).setFailureReason(null));
        } catch (RuntimeException exception) {
            int nextCount = retries + 1;
            boolean terminal = nextCount >= MAX_RETRIES;
            notificationMapper.updateById(notification.setRetryCount(nextCount)
                    .setStatus(terminal ? NotificationStatusEnum.FAILED : NotificationStatusEnum.PENDING)
                    .setNextRetryTime(terminal ? null : now.plusMinutes(1L << retries))
                    .setFailureReason(safeFailureReason(exception)));
        }
    }

    /**
     * 按用户语言和时区渲染通知模板。
     *
     * @param template  通知模板内容
     * @param variables 通知模板变量
     * @param language  语言标识
     * @param zone      用户时区
     * @return 方法处理后的结果
     */
    private String render(String template, Map<String, Object> variables, UserLanguageEnum language, String zone) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof java.time.Instant instant) {
                value = formatDate(instant, language, zone);
            }
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(value));
        }
        return result;
    }

    /**
     * 按用户语言和时区格式化日期时间。
     *
     * @param value    权益时长数值
     * @param language 语言标识
     * @param zone     用户时区
     * @return 方法处理后的结果
     */
    private String formatDate(java.time.Instant value, UserLanguageEnum language, String zone) {
        Locale locale = language == UserLanguageEnum.ZH_CN ? Locale.SIMPLIFIED_CHINESE
                : language == UserLanguageEnum.DE ? Locale.GERMAN : Locale.ENGLISH;
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale)
                .format(value.atZone(ZoneId.of(zone))).replace('\u00a0', ' ').replace('\u202f', ' ');
    }

    /**
     * 移除通知变量中的敏感信息。
     *
     * @param variables 通知模板变量
     * @return 方法处理后的结果
     */
    private Map<String, Object> redactSensitive(Map<String, Object> variables) {
        Map<String, Object> copy = new HashMap<>(variables);
        copy.replaceAll((key, value) -> isSensitive(key) ? "[REDACTED]" : value);
        return copy;
    }

    /**
     * 判断变量名称是否属于敏感字段。
     *
     * @param key 对象存储文件键
     * @return 操作是否成功
     */
    private boolean isSensitive(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("code") || normalized.contains("jwt") || normalized.contains("token")
                || normalized.contains("secret") || normalized.contains("key");
    }

    /**
     * 提取异常的安全错误原因，避免泄露敏感信息。
     *
     * @param exception 处理异常
     * @return 方法处理后的结果
     */
    private String safeFailureReason(RuntimeException exception) {
        return exception.getClass().getSimpleName();
    }
}
