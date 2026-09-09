package com.app.records;

import java.time.Instant;

/**
 * 用户本地日期对应的UTC时间范围。
 *
 * @param start UTC起始时间，包含
 * @param endExclusive UTC结束时间，不包含
 * @author yanlei
 * @since 2026-09-09
 */
public record UtcTimeRange(Instant start, Instant endExclusive) {
}
