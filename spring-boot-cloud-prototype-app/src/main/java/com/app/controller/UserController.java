package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.UpdateLanguageRequest;
import com.app.pojo.vo.UserVo;
import com.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料控制器。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 查询当前登录用户的资料。
     *
     * @return 当前用户资料
     */
    @GetMapping("/getProfile")
    public CommonResult<UserVo> getProfile() {
        return CommonResult.success(userService.getProfile());
    }

    /**
     * 更新当前登录用户的语言偏好。
     *
     * @param request 用户语言偏好请求
     * @return 更新后的用户资料
     */
    @PostMapping("/updateLanguage")
    public CommonResult<UserVo> updateLanguage(@Valid @RequestBody UpdateLanguageRequest request) {
        return CommonResult.success(userService.updateLanguage(request));
    }
}
