package com.app.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 认证验证码校验请求。 */
@Data
public class VerifyCodeRequest {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp="\\d{6}") private String code;
}
