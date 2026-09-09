package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 相册照片文件响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class PhotoVo {
    /** 原图照片文件记录主键ID。 */
    private Long id;

    /** 原始文件名称。 */
    private String fileName;

    /** 文件版本类型。 */
    private String fileType;

    /** 缩略图对象存储预签名访问地址。 */
    private String thumbnailUrl;

    /** 原图上传完成时间。 */
    private LocalDateTime uploadedTime;
}
