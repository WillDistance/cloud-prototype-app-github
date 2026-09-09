package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新用户语言偏好请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class UpdateLanguageRequest {
    /** 用户选择的语言代码。 */
    @NotBlank
    private String language;
}
