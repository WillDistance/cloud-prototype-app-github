package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.LanguageUpdateRequest;
import com.app.pojo.vo.UserProfileVo;
import com.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户设置控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private AuthService authService;

    /**
     * 查询当前登录用户资料。
     *
     * @return 当前用户资料
     */
    @GetMapping("/getProfile")
    public CommonResult<UserProfileVo> getProfile() {
        return CommonResult.success(authService.getProfile());
    }

    /**
     * 更新当前用户的通知语言偏好。
     *
     * @param request 语言偏好更新请求
     * @return 空数据成功响应
     */
    @PostMapping("/updateLanguage")
    public CommonResult<Void> updateLanguage(@Valid @RequestBody LanguageUpdateRequest request) {
        authService.updateLanguage(request);
        return CommonResult.success(null);
    }

}
