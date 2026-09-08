package com.app.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 重置密码请求。 */
@Data
public class ResetPasswordRequest {
    @NotBlank @Email private String email;
    @NotBlank @Size(min=8, max=128) private String password;
    @NotBlank private String confirmPassword;
    @NotBlank @Size(min=6, max=6) private String code;
}
