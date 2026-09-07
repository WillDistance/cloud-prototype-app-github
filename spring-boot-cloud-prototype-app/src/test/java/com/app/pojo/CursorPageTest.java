package com.app.pojo;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 游标分页模型测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class CursorPageTest {

    @Test
    void shouldCreateFirstPageRequestWithBoundedSize() {
        CursorPageRequest request = CursorPageRequest.firstPage(20);

        assertEquals(20, request.getSize());
        assertFalse(request.hasCursor());
        assertThrows(IllegalArgumentException.class, () -> CursorPageRequest.firstPage(0));
        assertThrows(IllegalArgumentException.class, () -> CursorPageRequest.firstPage(101));
    }

    @Test
    void shouldPreserveStableTimeAndIdCursor() {
        Instant time = Instant.parse("2026-09-06T01:02:03.456Z");
        CursorPageRequest request = CursorPageRequest.after(time, 99L, 10);

        assertTrue(request.hasCursor());
        assertEquals(time, request.getCursorTime());
        assertEquals(99L, request.getCursorId());
    }

    @Test
    void shouldExposeNextCursorOnlyWhenMoreDataExists() {
        Instant time = Instant.parse("2026-09-06T01:02:03.456Z");
        CursorPageResult<String> result = CursorPageResult.of(List.of("a", "b"), true, time, 8L);

        assertEquals(List.of("a", "b"), result.getItems());
        assertTrue(result.isHasMore());
        assertEquals(time, result.getNextCursorTime());
        assertEquals(8L, result.getNextCursorId());
    }
}
