package com.app.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 认证请求数据传输对象
 *
 * @author yanlei
 * @since 2026-09-06
 */
public final class AuthDto {
    private AuthDto() { }

    /** 注册验证码发送参数 */
    @Data
    @Accessors(chain = true)
    public static class SendRegisterCode {
        @NotBlank
        @Pattern(regexp = "^\\s*[^@\\s]+@[^@\\s]+\\.[^@\\s]+\\s*$")
        @Size(max = 320)
        private String email;
    }

    /** 注册验证码验证参数 */
    @Data
    @Accessors(chain = true)
    public static class VerifyRegisterCode {
        @NotBlank
        @Pattern(regexp = "^\\s*[^@\\s]+@[^@\\s]+\\.[^@\\s]+\\s*$")
        @Size(max = 320)
        private String email;
        @NotBlank
        @Pattern(regexp = "\\d{6}")
        private String code;
    }

    /** 用户注册参数 */
    @Data
    @Accessors(chain = true)
    public static class Register {
        @NotBlank
        @Pattern(regexp = "^\\s*[^@\\s]+@[^@\\s]+\\.[^@\\s]+\\s*$")
        @Size(max = 320)
        private String email;
        @NotBlank
        @Size(min = 6, max = 72)
        private String password;
        @NotBlank
        @Size(min = 6, max = 72)
        private String confirmPassword;
    }

    /** 登录参数 */
    @Data @Accessors(chain = true)
    public static class Login {
        @NotBlank @Email @Size(max = 320)
        private String email;
        @NotBlank @Size(min = 6, max = 72)
        private String password;
    }

    /** 重置密码验证码发送参数 */
    @Data @Accessors(chain = true)
    public static class SendResetPasswordCode {
        @NotBlank @Email @Size(max = 320)
        private String email;
    }

    /** 重置密码验证码验证参数 */
    @Data @Accessors(chain = true)
    public static class VerifyResetPasswordCode {
        @NotBlank @Email @Size(max = 320)
        private String email;
        @NotBlank @Pattern(regexp = "\\d{6}")
        private String code;
    }

    /** 重置密码参数 */
    @Data @Accessors(chain = true)
    public static class ResetPassword {
        @NotBlank @Email @Size(max = 320)
        private String email;
        @NotBlank @Size(min = 6, max = 72)
        private String newPassword;
        @NotBlank @Size(min = 6, max = 72)
        private String confirmPassword;
    }
}
