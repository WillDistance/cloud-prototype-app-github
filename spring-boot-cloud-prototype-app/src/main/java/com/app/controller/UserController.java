package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.UserDto;
import com.app.pojo.vo.UserProfileVo;
import com.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private UserService userService;

    /**
     * 获取JWT当前用户资料。
     *
     * @return 当前用户资料
     */
    @GetMapping("/getProfile")
    public CommonResult<UserProfileVo> getProfile() {
        return CommonResult.success(userService.getProfile());
    }

    /**
     * 更新JWT当前用户的通知语言偏好。
     *
     * @param dto 语言偏好请求
     * @return 更新后的当前用户资料
     */
    @PostMapping("/updateLanguage")
    public CommonResult<UserProfileVo> updateLanguage(@Valid @RequestBody UserDto.UpdateLanguage dto) {
        return CommonResult.success(userService.updateLanguage(dto));
    }
}
