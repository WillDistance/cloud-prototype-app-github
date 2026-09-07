package com.app.service;

import com.app.pojo.dto.UserDto;
import com.app.pojo.vo.UserProfileVo;

/**
 * 用户设置服务
 *
 * @author yanlei
 * @since 2026-09-06
 */
public interface UserService {
    /**
     * 查询当前登录用户的资料。
     *
     * @return 当前用户资料
     */
    UserProfileVo getProfile();

    /**
     * 更新当前登录用户的语言偏好。
     *
     * @param dto 语言偏好请求
     * @return 更新后的用户资料
     */
    UserProfileVo updateLanguage(UserDto.UpdateLanguage dto);
}
