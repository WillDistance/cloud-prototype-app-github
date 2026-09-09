package com.app.records;

import java.time.LocalDateTime;

/**
 * 。日期区间
 *
 * @param startTime 开始日期
 * @param endTime   结束日期
 */
public record DateRange(LocalDateTime startTime, LocalDateTime endTime) {
}