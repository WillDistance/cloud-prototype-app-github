package com.app.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

/**
 * 相册请求模型
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class PhotoDto {
    private PhotoDto() {
    }

    /** 相册列表查询参数。 */
    @Data
    @Accessors(chain = true)
    public static class ListQuery {
        private String filter = "ALL";
        private Instant cursorTime;
        private Long cursorId;
        private Integer size = 20;
    }

    /** 批量永久删除照片请求。 */
    @Data
    @Accessors(chain = true)
    public static class DeleteRequest {
        @NotEmpty
        private List<String> photoNos;
    }
}
