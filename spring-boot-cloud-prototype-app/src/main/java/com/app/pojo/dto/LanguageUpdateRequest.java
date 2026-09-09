package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 用户语言偏好更新请求。 */
@Data
public class LanguageUpdateRequest {
    /** 语言代码，例如zh-CN、en或de。 */
    @NotBlank
    @Size(max = 16)
    private String language;
}
