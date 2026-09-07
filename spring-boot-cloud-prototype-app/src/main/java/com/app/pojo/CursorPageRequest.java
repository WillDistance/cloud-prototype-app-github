package com.app.pojo;

import lombok.Getter;

import java.time.Instant;

/**
 * 游标分页请求
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Getter
public final class CursorPageRequest {

    public static final int MAX_PAGE_SIZE = 100;

    private final Instant cursorTime;
    private final Long cursorId;
    private final int size;

    private CursorPageRequest(Instant cursorTime, Long cursorId, int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("分页大小必须在1到100之间");
        }
        if ((cursorTime == null) != (cursorId == null)) {
            throw new IllegalArgumentException("游标时间和游标ID必须同时提供");
        }
        if (cursorId != null && cursorId <= 0) {
            throw new IllegalArgumentException("游标ID必须大于0");
        }
        this.cursorTime = cursorTime;
        this.cursorId = cursorId;
        this.size = size;
    }

    public static CursorPageRequest firstPage(int size) {
        return new CursorPageRequest(null, null, size);
    }

    public static CursorPageRequest after(Instant cursorTime, Long cursorId, int size) {
        return new CursorPageRequest(cursorTime, cursorId, size);
    }

    public boolean hasCursor() {
        return cursorTime != null;
    }
}
