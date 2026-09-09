package com.app.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * UTC时间与用户时区转换工具。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public final class UtcTimeUtil {
    private UtcTimeUtil() {
    }

    /**
     * 计算用户本地日期对应的UTC起始时间。
     *
     * @param date 用户所在时区的本地日期
     * @param zone 用户IANA时区
     * @return UTC起始时间
     */
    public static Instant startOfDayUtc(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    /**
     * 计算用户本地日期下一天对应的UTC起始时间，用作左闭右开区间的结束边界。
     *
     * @param date 用户所在时区的本地日期
     * @param zone 用户IANA时区
     * @return UTC结束边界
     */
    public static Instant startOfNextDayUtc(LocalDate date, ZoneId zone) {
        return date.plusDays(1).atStartOfDay(zone).toInstant();
    }

    /**
     * 将UTC本地日期时间转换为指定时区的本地日期时间。
     *
     * @param value UTC本地日期时间
     * @param zone 目标IANA时区
     * @return 目标时区本地日期时间
     */
    public static LocalDateTime toUserTime(LocalDateTime value, ZoneId zone) {
        return value.atOffset(ZoneOffset.UTC).atZoneSameInstant(zone).toLocalDateTime();
    }
}
