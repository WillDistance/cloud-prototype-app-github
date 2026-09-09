package com.app.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册照片游标查询请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class PhotoListRequest {
    /** 时间筛选范围：TODAY、SEVEN_DAYS、ONE_MONTH或ALL。 */
    private String range = "ALL";

    /** 上一页最后一条照片的上传时间。 */
    private LocalDateTime cursorTime;

    /** 上一页最后一条照片的主键ID。 */
    private Long cursorId;

    /** 每页返回数量，默认20，最大50。 */
    private Integer size = 20;
}
