package com.app.test;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import com.app.exception.BusinessException;
import com.app.exception.RequestParameterException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

/**
 * 异常映射测试控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/test-exception")
public class TestExceptionController {

    @GetMapping("/business")
    public void business() {
        throw new BusinessException(ErrorCodeEnum.AUTH_INVALID_CREDENTIALS);
    }

    @GetMapping("/authentication")
    public void authentication() {
        throw new AuthenticationException(ErrorCodeEnum.AUTH_TOKEN_EXPIRED);
    }

    @GetMapping("/parameter")
    public void parameter() {
        throw new RequestParameterException("测试参数错误");
    }

    @PostMapping("/validation")
    public void validation(@Valid @RequestBody ValidationRequest request) {
    }

    @GetMapping("/system")
    public void system() {
        throw new IllegalStateException("数据库内部细节");
    }

    /**
     * 参数校验测试请求
     *
     * @param name 名称
     */
    public record ValidationRequest(@NotBlank String name) {
    }
}
