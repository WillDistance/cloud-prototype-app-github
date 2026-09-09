package com.app;

import com.app.enums.LanguageEnum;
import com.app.records.UtcTimeRange;
import com.app.utils.UserTimeRangeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段五用户语言和时区规则测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase5UserProfileTest {
    @Test
    void 支持系统定义的三种通知语言() {
        assertEquals("zh-CN", LanguageEnum.ZH_CN.getValue());
        assertEquals("en", LanguageEnum.EN.getValue());
        assertEquals("de", LanguageEnum.DE.getValue());
    }

    @Test
    void 上海时区日期范围转换为UTC边界() {
        UtcTimeRange range = UserTimeRangeUtil.day(LocalDate.of(2026, 9, 9), ZoneId.of("Asia/Shanghai"));

        assertEquals("2026-09-08T16:00:00Z", range.start().toString());
        assertEquals("2026-09-09T16:00:00Z", range.endExclusive().toString());
    }

    @Test
    void 夏令时切换日仍按本地自然日计算() {
        UtcTimeRange range = UserTimeRangeUtil.day(LocalDate.of(2026, 3, 29), ZoneId.of("Europe/Berlin"));

        assertEquals(23, java.time.Duration.between(range.start(), range.endExclusive()).toHours());
        assertTrue(range.endExclusive().isAfter(range.start()));
    }

    @Test
    void 月末和闰年自然月边界计算正确() {
        UtcTimeRange range = UserTimeRangeUtil.month(YearMonth.of(2028, 2), ZoneId.of("Asia/Shanghai"));

        assertEquals("2028-01-31T16:00:00Z", range.start().toString());
        assertEquals("2028-02-29T16:00:00Z", range.endExclusive().toString());
    }
}
