package com.app.utils;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.RequestParameterException;

import java.time.*;

/**
 * 用户时区和自然时间计算工具
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class UserTimeZoneUtil {

    private static final String UTC = "UTC";

    /**
     * 处理UserTimeZoneUtil相关的业务逻辑。
     *
     * @return 处理结果
     */
    private UserTimeZoneUtil() {
    }

    public static boolean isValidIanaZone(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return false;
        }
        if (!UTC.equals(zoneId) && !zoneId.contains("/")) {
            return false;
        }
        try {
            ZoneId.of(zoneId);
            return true;
        } catch (DateTimeException exception) {
            return false;
        }
    }

    public static ZoneId requireIanaZone(String zoneId) {
        if (!isValidIanaZone(zoneId)) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        return ZoneId.of(zoneId);
    }

    public static UtcTimeRange toUtcDayRange(LocalDate localDate, String zoneId) {
        if (localDate == null) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
        ZoneId userZone = requireIanaZone(zoneId);
        Instant startUtc = localDate.atStartOfDay(userZone).toInstant();
        Instant endUtc = localDate.plusDays(1).atStartOfDay(userZone).toInstant();
        return new UtcTimeRange(startUtc, endUtc);
    }

    public static Instant calculateNaturalMonthExpiry(Instant effectiveTime, String zoneId, long months) {
        validateExpiryArguments(effectiveTime, months);
        ZoneId userZone = requireIanaZone(zoneId);
        return ZonedDateTime.ofInstant(effectiveTime, userZone).plusMonths(months).toInstant();
    }

    public static Instant calculateNaturalYearExpiry(Instant effectiveTime, String zoneId, long years) {
        validateExpiryArguments(effectiveTime, years);
        ZoneId userZone = requireIanaZone(zoneId);
        return ZonedDateTime.ofInstant(effectiveTime, userZone).plusYears(years).toInstant();
    }

    /**
     * 处理validateExpiryArguments相关的业务逻辑。
     *
     * @param effectiveTime 方法参数（effectiveTime）
     * @param amount        方法参数（amount）
     * @return 处理结果
     */
    private static void validateExpiryArguments(Instant effectiveTime, long amount) {
        if (effectiveTime == null || amount <= 0) {
            throw new RequestParameterException(ErrorCodeEnum.INVALID_REQUEST_PARAMETER);
        }
    }
}
