package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户设置请求参数
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class UserDto {
    private UserDto() { }

    /**
     * 更新通知语言偏好请求。
     *
     * @author yanlei
     * @since 2026-09-06
     */
    @Data
    @Accessors(chain = true)
    public static class UpdateLanguage {
        @NotBlank
        private String language;
    }
}
