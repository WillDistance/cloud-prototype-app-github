package com.app.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除照片请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class DeletePhotosRequest {
    /** 待删除照片文件记录主键ID，最多9条。 */
    @NotEmpty
    @Size(max = 9)
    private List<Long> photoFileIds;
}
