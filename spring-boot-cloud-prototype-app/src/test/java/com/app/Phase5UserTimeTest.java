package com.app;

import com.app.utils.UtcTimeUtil;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 阶段5用户时区和UTC时间规则测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase5UserTimeTest {
    /**
     * 验证上海自然日转换为UTC后仍使用左闭右开的一整天区间。
     */
    @Test
    void shouldConvertShanghaiDayToUtcRange() {
        var start = UtcTimeUtil.startOfDayUtc(LocalDate.of(2026, 9, 9), ZoneId.of("Asia/Shanghai"));
        var end = UtcTimeUtil.startOfNextDayUtc(LocalDate.of(2026, 9, 9), ZoneId.of("Asia/Shanghai"));
        assertEquals(LocalDate.of(2026, 9, 8).atTime(16, 0).toInstant(ZoneOffset.UTC), start);
        assertEquals(Duration.ofHours(24), Duration.between(start, end));
    }

    /**
     * 验证夏令时切换日按真实时区规则计算，而不是固定按24小时计算。
     */
    @Test
    void shouldHandleDaylightSavingTime() {
        var start = UtcTimeUtil.startOfDayUtc(LocalDate.of(2026, 3, 29), ZoneId.of("Europe/Berlin"));
        var end = UtcTimeUtil.startOfNextDayUtc(LocalDate.of(2026, 3, 29), ZoneId.of("Europe/Berlin"));
        assertEquals(Duration.ofHours(23), Duration.between(start, end));
    }
}
