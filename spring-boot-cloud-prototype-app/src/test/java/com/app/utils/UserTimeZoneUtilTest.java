package com.app.utils;

import com.app.exception.RequestParameterException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户时区工具测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class UserTimeZoneUtilTest {

    @Test
    void shouldValidateIanaRegionAndUtcZone() {
        assertTrue(UserTimeZoneUtil.isValidIanaZone("UTC"));
        assertTrue(UserTimeZoneUtil.isValidIanaZone("Asia/Shanghai"));
        assertTrue(UserTimeZoneUtil.isValidIanaZone("America/New_York"));
        assertFalse(UserTimeZoneUtil.isValidIanaZone("GMT+08:00"));
        assertFalse(UserTimeZoneUtil.isValidIanaZone("+08:00"));
        assertFalse(UserTimeZoneUtil.isValidIanaZone("Invalid/Zone"));
        assertThrows(RequestParameterException.class, () -> UserTimeZoneUtil.requireIanaZone("Invalid/Zone"));
    }

    @Test
    void shouldConvertUtcNaturalDayToUtcRange() {
        UtcTimeRange range = UserTimeZoneUtil.toUtcDayRange(LocalDate.of(2026, 9, 6), "UTC");

        assertEquals(Instant.parse("2026-09-06T00:00:00Z"), range.getStartUtc());
        assertEquals(Instant.parse("2026-09-07T00:00:00Z"), range.getEndUtc());
    }

    @Test
    void shouldConvertDstSpringDayToTwentyThreeHourRange() {
        UtcTimeRange range = UserTimeZoneUtil.toUtcDayRange(LocalDate.of(2026, 3, 8), "America/New_York");

        assertEquals(Instant.parse("2026-03-08T05:00:00Z"), range.getStartUtc());
        assertEquals(Instant.parse("2026-03-09T04:00:00Z"), range.getEndUtc());
        assertEquals(23, range.duration().toHours());
    }

    @Test
    void shouldConvertDstFallDayToTwentyFiveHourRange() {
        UtcTimeRange range = UserTimeZoneUtil.toUtcDayRange(LocalDate.of(2026, 11, 1), "America/New_York");

        assertEquals(Instant.parse("2026-11-01T04:00:00Z"), range.getStartUtc());
        assertEquals(Instant.parse("2026-11-02T05:00:00Z"), range.getEndUtc());
        assertEquals(25, range.duration().toHours());
    }

    @Test
    void shouldCalculateNaturalMonthExpiryAtLocalCalendarBoundary() {
        ZonedDateTime effective = ZonedDateTime.parse("2026-01-31T10:15:00+08:00[Asia/Shanghai]");

        Instant expiry = UserTimeZoneUtil.calculateNaturalMonthExpiry(effective.toInstant(), "Asia/Shanghai", 1);

        assertEquals(Instant.parse("2026-02-28T02:15:00Z"), expiry);
    }

    @Test
    void shouldCalculateNaturalYearExpiryForLeapDay() {
        ZonedDateTime effective = ZonedDateTime.parse("2024-02-29T08:30:00+08:00[Asia/Shanghai]");

        Instant expiry = UserTimeZoneUtil.calculateNaturalYearExpiry(effective.toInstant(), "Asia/Shanghai", 1);

        assertEquals(Instant.parse("2025-02-28T00:30:00Z"), expiry);
    }

    @Test
    void shouldKeepLocalWallClockAcrossDstWhenAddingNaturalMonth() {
        ZonedDateTime effective = ZonedDateTime.parse("2026-02-15T10:00:00-05:00[America/New_York]");

        Instant expiry = UserTimeZoneUtil.calculateNaturalMonthExpiry(effective.toInstant(), "America/New_York", 1);

        assertEquals(Instant.parse("2026-03-15T14:00:00Z"), expiry);
    }
}
