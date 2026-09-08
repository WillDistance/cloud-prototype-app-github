package com.app.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 认证验证码请求。 */
@Data
public class AuthEmailCodeRequest {
    @NotBlank @Email private String email;
}
