package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OSS回调处理结果
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class OssCallbackVo {
    private String uploadNo;
    private String status;
    private String photoNo;
    private Boolean idempotent;
}
