package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 照片详情响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class PhotoDetailVo {
    /** 原图照片文件记录主键ID。 */
    private Long id;

    /** 原始文件名称。 */
    private String fileName;

    /** 预览图临时访问地址。 */
    private String previewUrl;

    /** 原图上传完成时间。 */
    private LocalDateTime uploadedTime;
}
