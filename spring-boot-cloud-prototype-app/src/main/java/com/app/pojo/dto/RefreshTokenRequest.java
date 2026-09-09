package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh Token刷新请求。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Data
public class RefreshTokenRequest {
    /** Refresh Token。 */
    @NotBlank
    private String refreshToken;
}
