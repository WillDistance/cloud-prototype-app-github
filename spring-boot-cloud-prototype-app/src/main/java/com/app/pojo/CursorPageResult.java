package com.app.pojo;

import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * 游标分页结果
 *
 * @param <T> 数据类型
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
public final class CursorPageResult<T> {

    private final List<T> items;
    private final boolean hasMore;
    private final Instant nextCursorTime;
    private final Long nextCursorId;

    private CursorPageResult(List<T> items, boolean hasMore, Instant nextCursorTime, Long nextCursorId) {
        if (hasMore && (nextCursorTime == null || nextCursorId == null)) {
            throw new IllegalArgumentException("存在下一页时必须提供下一游标");
        }
        this.items = List.copyOf(items);
        this.hasMore = hasMore;
        this.nextCursorTime = hasMore ? nextCursorTime : null;
        this.nextCursorId = hasMore ? nextCursorId : null;
    }

    public static <T> CursorPageResult<T> of(List<T> items, boolean hasMore,
                                             Instant nextCursorTime, Long nextCursorId) {
        return new CursorPageResult<>(items, hasMore, nextCursorTime, nextCursorId);
    }

    public static <T> CursorPageResult<T> lastPage(List<T> items) {
        return new CursorPageResult<>(items, false, null, null);
    }
}
