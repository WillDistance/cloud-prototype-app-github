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
    private Long id;
    private String fileName;
    private String fileType;
    private String objectKey;
    private LocalDateTime uploadedTime;
}
