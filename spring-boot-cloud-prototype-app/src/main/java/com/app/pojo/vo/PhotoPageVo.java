package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 相册游标分页响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class PhotoPageVo {
    /** 当前页照片列表。 */
    private List<PhotoVo> items;

    /** 是否存在下一页。 */
    private boolean hasMore;

    /** 下一页游标对应的上传时间。 */
    private String nextCursorTime;

    /** 下一页游标对应的照片文件主键ID。 */
    private Long nextCursorId;
}
