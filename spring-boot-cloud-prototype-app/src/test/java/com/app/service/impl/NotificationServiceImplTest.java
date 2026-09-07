package com.app.service.impl;

import com.app.enums.NotificationChannelEnum;
import com.app.enums.NotificationStatusEnum;
import com.app.enums.NotificationTypeEnum;
import com.app.enums.UserLanguageEnum;
import com.app.mapper.NotificationMapper;
import com.app.mapper.UserMapper;
import com.app.pojo.entity.NotificationEntity;
import com.app.pojo.entity.UserEntity;
import com.app.support.notification.ClasspathNotificationTemplateProvider;
import com.app.support.notification.NotificationMessage;
import com.app.support.notification.NotificationSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 6, 12, 0);

    @Test
    void shouldRenderFiveTemplatesInChineseGermanAndEnglish() {
        for (UserLanguageEnum language : UserLanguageEnum.values()) {
            NotificationMapper mapper = mock(NotificationMapper.class);
            UserMapper users = mock(UserMapper.class);
            when(users.selectById(7L)).thenReturn(user(language, "Asia/Shanghai"));
            NotificationServiceImpl service = service(mapper, users, mock(NotificationSender.class));
            for (NotificationTypeEnum type : NotificationTypeEnum.values()) {
                NotificationEntity result = service.create(7L, "user@example.com", type,
                        NotificationChannelEnum.EMAIL, language.name() + "-" + type.name(), variables());
                assertNotNull(result.getSubject());
                assertFalse(result.getSubject().isBlank());
                assertFalse(result.getContent().isBlank());
                assertEquals(language, result.getLanguageCode());
                verify(mapper).insert(result);
            }
        }
    }

    @Test
    void shouldFallbackToEnglishWhenUserLanguageIsMissing() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        UserMapper users = mock(UserMapper.class);
        when(users.selectById(7L)).thenReturn(user(null, "UTC"));

        NotificationEntity result = service(mapper, users, mock(NotificationSender.class)).create(7L,
                "user@example.com", NotificationTypeEnum.EXPIRY_REMINDER, NotificationChannelEnum.EMAIL,
                "fallback-1", Map.of("expiryDate", Instant.parse("2026-09-06T12:00:00Z")));

        assertEquals(UserLanguageEnum.EN, result.getLanguageCode());
        assertEquals("Storage entitlement expiry reminder", result.getSubject());
    }

    @Test
    void shouldFormatInstantInUserIanaTimeZone() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        UserMapper users = mock(UserMapper.class);
        when(users.selectById(7L)).thenReturn(user(UserLanguageEnum.EN, "America/Los_Angeles"));

        NotificationEntity result = service(mapper, users, mock(NotificationSender.class)).create(7L,
                "user@example.com", NotificationTypeEnum.CLEANUP_STARTED, NotificationChannelEnum.EMAIL,
                "date-1", Map.of("eventDate", Instant.parse("2026-09-06T12:00:00Z")));

        assertEquals("Automatic cleanup started at Sep 6, 2026, 5:00:00 AM.", result.getContent());
    }

    @Test
    void shouldReturnExistingNotificationForSameIdempotencyKeyWithoutInsert() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        NotificationEntity existing = new NotificationEntity().setId(9L).setIdempotencyKey("same-key");
        when(mapper.selectOne(any())).thenReturn(existing);

        NotificationEntity result = service(mapper, mock(UserMapper.class), mock(NotificationSender.class)).create(
                7L, "user@example.com", NotificationTypeEnum.VERIFY_CODE, NotificationChannelEnum.EMAIL,
                "same-key", Map.of("code", "123456"));

        assertEquals(existing, result);
        verify(mapper, never()).insert(any(NotificationEntity.class));
    }

    @Test
    void shouldMarkNotificationSentAfterSuccessfulSend() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        NotificationSender sender = mock(NotificationSender.class);
        NotificationEntity notification = pending(1L, 0);
        when(mapper.selectById(1L)).thenReturn(notification);

        service(mapper, mock(UserMapper.class), sender).send(1L, NOW);

        assertEquals(NotificationStatusEnum.SENT, notification.getStatus());
        assertEquals(NOW, notification.getSentTime());
        verify(sender).send(any(NotificationMessage.class));
    }

    @Test
    void shouldReturnPendingWithExponentialBackoffAfterFirstAndSecondFailures() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        NotificationSender sender = mock(NotificationSender.class);
        NotificationEntity notification = pending(1L, 0);
        when(mapper.selectById(1L)).thenReturn(notification);
        doThrow(new RuntimeException("temporary")).when(sender).send(any());
        NotificationServiceImpl service = service(mapper, mock(UserMapper.class), sender);

        service.send(1L, NOW);
        assertEquals(NotificationStatusEnum.PENDING, notification.getStatus());
        assertEquals(1, notification.getRetryCount());
        assertEquals(NOW.plusMinutes(1), notification.getNextRetryTime());
        service.send(1L, NOW);
        assertEquals(NotificationStatusEnum.PENDING, notification.getStatus());
        assertEquals(2, notification.getRetryCount());
        assertEquals(NOW.plusMinutes(2), notification.getNextRetryTime());
    }

    @Test
    void shouldMarkFailedAfterThirdFailure() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        NotificationSender sender = mock(NotificationSender.class);
        NotificationEntity notification = pending(1L, 2);
        when(mapper.selectById(1L)).thenReturn(notification);
        doThrow(new RuntimeException("permanent")).when(sender).send(any());

        service(mapper, mock(UserMapper.class), sender).send(1L, NOW);

        assertEquals(NotificationStatusEnum.FAILED, notification.getStatus());
        assertEquals(3, notification.getRetryCount());
        assertEquals(null, notification.getNextRetryTime());
    }

    @Test
    void shouldRedactSensitiveVariablesFromNotificationBodyAndSenderMessage() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        UserMapper users = mock(UserMapper.class);
        NotificationSender sender = mock(NotificationSender.class);
        when(users.selectById(7L)).thenReturn(user(UserLanguageEnum.EN, "UTC"));
        NotificationEntity notification = service(mapper, users, sender).create(7L, "u@example.com",
                NotificationTypeEnum.VERIFY_CODE, NotificationChannelEnum.EMAIL, "safe-1",
                Map.of("code", "123456", "jwtToken", "jwt-secret", "expiryDate", Instant.parse("2026-09-06T12:00:00Z")));
        when(mapper.selectById(4L)).thenReturn(notification.setId(4L));

        service(mapper, users, sender).send(4L, NOW);

        assertFalse(notification.getContent().contains("123456"));
        assertFalse(notification.getContent().contains("jwt-secret"));
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(sender).send(captor.capture());
        assertFalse(captor.getValue().content().contains("123456"));
    }

    @Test
    void shouldRejectInvalidTimeZoneBeforeInsert() {
        NotificationMapper mapper = mock(NotificationMapper.class);
        UserMapper users = mock(UserMapper.class);
        when(users.selectById(7L)).thenReturn(user(UserLanguageEnum.EN, "Not/AZone"));

        assertThrows(RuntimeException.class, () -> service(mapper, users, mock(NotificationSender.class)).create(
                7L, "u@example.com", NotificationTypeEnum.VERIFY_CODE, NotificationChannelEnum.EMAIL,
                "bad-zone", Map.of("code", "123456")));
        verify(mapper, never()).insert(any(NotificationEntity.class));
    }

    private static NotificationServiceImpl service(NotificationMapper mapper, UserMapper users,
                                                    NotificationSender sender) {
        return new NotificationServiceImpl(mapper, users, new ClasspathNotificationTemplateProvider(), sender);
    }

    private static UserEntity user(UserLanguageEnum language, String zone) {
        return new UserEntity().setId(7L).setPreferredLanguage(language).setTimeZone(zone);
    }

    private static Map<String, Object> variables() {
        return Map.of("code", "123456", "expiryDate", Instant.parse("2026-09-06T12:00:00Z"),
                "eventDate", Instant.parse("2026-09-06T12:00:00Z"), "deletedCount", 3,
                "paymentStatus", "PAID", "minutes", 10);
    }

    private static NotificationEntity pending(Long id, int retries) {
        return new NotificationEntity().setId(id).setChannel(NotificationChannelEnum.EMAIL)
                .setRecipient("u@example.com").setSubject("subject").setContent("content")
                .setStatus(NotificationStatusEnum.PENDING).setRetryCount(retries);
    }
}
