package com.app.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询照片详情请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class PhotoDetailRequest {
    /** 照片文件记录主键ID。 */
    @NotNull
    private Long photoFileId;
}
