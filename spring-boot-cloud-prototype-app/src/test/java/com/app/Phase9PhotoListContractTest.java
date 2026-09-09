package com.app;

import com.app.enums.PhotoRangeEnum;
import com.app.pojo.dto.PhotoListRequest;
import com.app.pojo.vo.PhotoPageVo;
import com.app.service.PhotoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段9相册查询契约测试。
 *
 * @author yanlei
 * @since 2026-09-09
 */
class Phase9PhotoListContractTest {
    /**
     * 验证相册筛选范围包含需求规定的四种取值。
     */
    @Test
    void shouldProvideRequiredPhotoRanges() {
        assertEquals("TODAY", PhotoRangeEnum.TODAY.getValue());
        assertEquals("SEVEN_DAYS", PhotoRangeEnum.SEVEN_DAYS.getValue());
        assertEquals("ONE_MONTH", PhotoRangeEnum.ONE_MONTH.getValue());
        assertEquals("ALL", PhotoRangeEnum.ALL.getValue());
    }

    /**
     * 验证相册请求默认使用全部范围和合理分页大小。
     */
    @Test
    void shouldUseDefaultListOptions() {
        PhotoListRequest request = new PhotoListRequest();
        assertEquals("ALL", request.getRange());
        assertEquals(20, request.getSize());
    }
}
