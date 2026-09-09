package com.app.utils;

import com.app.records.UtcTimeRange;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.YearMonth;

/**
 * 用户本地日期与UTC时间范围转换工具。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public final class UserTimeRangeUtil {
    private UserTimeRangeUtil() {
    }

    /**
     * 将用户时区中的自然日转换为UTC半开区间。
     *
     * @param date 用户时区中的自然日期
     * @param zone 用户IANA时区
     * @return UTC起始时间包含、结束时间不包含的时间范围
     */
    public static UtcTimeRange day(LocalDate date, ZoneId zone) {
        ZonedDateTime start = date.atStartOfDay(zone);
        ZonedDateTime end = date.plusDays(1).atStartOfDay(zone);
        return new UtcTimeRange(start.toInstant(), end.toInstant());
    }

    /**
     * 将用户时区中的自然月转换为UTC半开区间。
     *
     * @param month 用户时区中的年月
     * @param zone 用户IANA时区
     * @return UTC起始时间包含、结束时间不包含的时间范围
     */
    public static UtcTimeRange month(YearMonth month, ZoneId zone) {
        ZonedDateTime start = month.atDay(1).atStartOfDay(zone);
        ZonedDateTime end = month.plusMonths(1).atDay(1).atStartOfDay(zone);
        return new UtcTimeRange(start.toInstant(), end.toInstant());
    }

    /**
     * 将用户时区中的自然年转换为UTC半开区间。
     *
     * @param year 用户时区中的年份
     * @param zone 用户IANA时区
     * @return UTC起始时间包含、结束时间不包含的时间范围
     */
    public static UtcTimeRange year(int year, ZoneId zone) {
        ZonedDateTime start = LocalDate.of(year, 1, 1).atStartOfDay(zone);
        ZonedDateTime end = LocalDate.of(year + 1, 1, 1).atStartOfDay(zone);
        return new UtcTimeRange(start.toInstant(), end.toInstant());
    }
}
