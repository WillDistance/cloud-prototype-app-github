package com.app.utils;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * UTC时间区间
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
public final class UtcTimeRange {

    private final Instant startUtc;
    private final Instant endUtc;

    public UtcTimeRange(Instant startUtc, Instant endUtc) {
        if (startUtc == null || endUtc == null || !startUtc.isBefore(endUtc)) {
            throw new IllegalArgumentException("UTC时间区间无效");
        }
        this.startUtc = startUtc;
        this.endUtc = endUtc;
    }

    public Duration duration() {
        return Duration.between(startUtc, endUtc);
    }
}
